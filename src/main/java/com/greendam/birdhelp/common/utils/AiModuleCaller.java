package com.greendam.birdhelp.common.utils;

import com.greendam.birdhelp.properties.AiModuleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * AI 模块调用客户端，对 /ai/** 接口发起带 RSA 签名的 HTTP 请求。
 *
 * <p>文档生成（PPT/Word/PDF）已迁移至 RabbitMQ 异步消息，不再通过本类调用。</p>
 *
 * <p>v5.2 新增对话修改代理方法（{@code chatModify} / {@code chatDiscuss}）。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Component
public class AiModuleCaller {

    @Resource
    private AiModuleProperties aiModuleProperties;

    private PrivateKey privateKey;
    private String baseUrl;
    private HttpClient httpClient;

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @PostConstruct
    public void init() {
        this.baseUrl = aiModuleProperties.getBaseUrl();
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        if (aiModuleProperties.getPrivateKey() != null && !aiModuleProperties.getPrivateKey().isBlank()) {
            this.privateKey = RsaSignUtil.loadPrivateKey(aiModuleProperties.getPrivateKey());
            log.info("AI 模块调用客户端已初始化: baseUrl={}", baseUrl);
        } else {
            log.warn("AI 模块私钥未配置（ai-module.private-key），对 AI 模块的调用将跳过。" +
                    "请在 application-dev.yml 中配置。");
        }
    }

    /**
     * 上传素材到 AI 模块并触发 RAG 摄取。
     */
    public void uploadMaterial(byte[] content, String fileName, Long userId, Long projectId, Long javaFileId) {
        if (!isReady()) {
            return;
        }
        try {
            Map<String, String> fields = Map.of(
                    "userId", String.valueOf(userId),
                    "projectId", String.valueOf(projectId),
                    "javaFileId", String.valueOf(javaFileId)
            );
            HttpResponse<String> resp = signedMultipartRequest(
                    "/ai/material/upload", fields,
                    "file", fileName, content, "application/octet-stream"
            );
            log.info("AI 素材上传完成: file={}, status={}, body={}", fileName, resp.statusCode(), resp.body());
        } catch (Exception e) {
            log.error("AI 素材上传失败: file={}, userId={}, projectId={}", fileName, userId, projectId, e);
        }
    }

    /**
     * 通知 AI 模块删除素材（软删除时调用）。
     */
    public void deleteMaterial(Long materialId, Long userId, Long projectId) {
        if (!isReady()) {
            return;
        }
        try {
            String path = "/ai/material/" + materialId + "?userId=" + userId + "&projectId=" + projectId;
            HttpResponse<String> resp = signedNoBodyRequest("DELETE", path);
            log.info("AI 素材删除完成: materialId={}, status={}, body={}", materialId, resp.statusCode(), resp.body());
        } catch (Exception e) {
            log.error("AI 素材删除失败: materialId={}, userId={}, projectId={}", materialId, userId, projectId, e);
        }
    }

    /**
     * 通知 AI 模块重建向量索引（回收站恢复时调用）。
     */
    public void reindexMaterial(Long materialId, Long userId, Long projectId, String fileName) {
        if (!isReady()) {
            return;
        }
        try {
            String path = "/ai/material/" + materialId + "/reindex?userId=" + userId
                    + "&projectId=" + projectId + "&fileName=" + urlEncode(fileName);
            HttpResponse<String> resp = signedNoBodyRequest("POST", path);
            log.info("AI 向量重建完成: materialId={}, status={}, body={}", materialId, resp.statusCode(), resp.body());
        } catch (Exception e) {
            log.error("AI 向量重建失败: materialId={}, userId={}, projectId={}", materialId, userId, projectId, e);
        }
    }

    /**
     * 代理调用 Python {@code POST /ai/chat/modify} 对话修改接口。
     *
     * <p>流程：前端 → 本方法（RSA 签名）→ Python modify 模块 → LLM 修改 + 重建文件 → 返回结果</p>
     *
     * @param requestBody 请求 JSON 字符串（由 ChatController 序列化传入）
     * @return Python 返回的响应体字符串（JSON 格式）
     * @throws Exception 网络异常或签名失败时抛出
     */
    public String chatModify(String requestBody) throws Exception {
        if (!isReady()) {
            return "{\"code\":50000,\"message\":\"AI 模块未配置私钥\",\"data\":null}";
        }
        HttpResponse<String> resp = signedJsonRequest("POST", "/ai/chat/modify", requestBody);
        log.info("AI 对话修改完成: status={}, bodyLen={}", resp.statusCode(),
                resp.body() != null ? resp.body().length() : 0);
        return resp.body();
    }

    /**
     * 代理调用 Python {@code POST /ai/chat/discuss} 对话讨论接口（仅问答，不重建文件）。
     *
     * @param requestBody 请求 JSON 字符串（由 ChatController 序列化传入）
     * @return Python 返回的响应体字符串（JSON 格式）
     * @throws Exception 网络异常或签名失败时抛出
     */
    public String chatDiscuss(String requestBody) throws Exception {
        if (!isReady()) {
            return "{\"code\":50000,\"message\":\"AI 模块未配置私钥\",\"data\":null}";
        }
        HttpResponse<String> resp = signedJsonRequest("POST", "/ai/chat/discuss", requestBody);
        log.info("AI 对话讨论完成: status={}, bodyLen={}", resp.statusCode(),
                resp.body() != null ? resp.body().length() : 0);
        return resp.body();
    }

    /**
     * 通知 AI 模块清理残留向量（永久删除时调用）。
     */
    public void purgeVector(Long materialId, Long userId, Long projectId) {
        if (!isReady()) {
            return;
        }
        try {
            String path = "/ai/material/" + materialId + "/vector-purge?userId=" + userId + "&projectId=" + projectId;
            HttpResponse<String> resp = signedNoBodyRequest("POST", path);
            log.info("AI 向量清理完成: materialId={}, status={}, body={}", materialId, resp.statusCode(), resp.body());
        } catch (Exception e) {
            log.error("AI 向量清理失败: materialId={}, userId={}, projectId={}", materialId, userId, projectId, e);
        }
    }

    // ==================== 底层 HTTP 签名方法 ====================

    private HttpResponse<String> signedJsonRequest(String method, String path, String jsonBody) throws Exception {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String nonce = UUID.randomUUID().toString();
        String signature = sign(method, path, jsonBody, timestamp, nonce);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("X-Timestamp", timestamp)
                .header("X-Nonce", nonce)
                .header("X-Signature", signature)
                .method(method, HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> signedNoBodyRequest(String method, String path) throws Exception {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String nonce = UUID.randomUUID().toString();
        String signature = sign(method, path, "", timestamp, nonce);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("X-Timestamp", timestamp)
                .header("X-Nonce", nonce)
                .header("X-Signature", signature);

        HttpRequest request;
        if ("DELETE".equalsIgnoreCase(method)) {
            request = builder.DELETE().build();
        } else if ("POST".equalsIgnoreCase(method)) {
            request = builder.POST(HttpRequest.BodyPublishers.noBody()).build();
        } else {
            request = builder.GET().build();
        }

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> signedMultipartRequest(
            String path, Map<String, String> fields,
            String fileFieldName, String fileName, byte[] fileContent,
            String fileContentType) throws Exception {

        String boundary = UUID.randomUUID().toString().replace("-", "");
        java.io.ByteArrayOutputStream bodyOs = new java.io.ByteArrayOutputStream();

        for (var entry : fields.entrySet()) {
            bodyOs.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            bodyOs.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            bodyOs.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
            bodyOs.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        bodyOs.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        bodyOs.write(("Content-Disposition: form-data; name=\"" + fileFieldName
                + "\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        bodyOs.write(("Content-Type: " + fileContentType + "\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        bodyOs.write(fileContent);
        bodyOs.write("\r\n".getBytes(StandardCharsets.UTF_8));
        bodyOs.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        byte[] bodyBytes = bodyOs.toByteArray();

        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String nonce = UUID.randomUUID().toString();
        String signature = signRaw("POST", path, bodyBytes, timestamp, nonce);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("X-Timestamp", timestamp)
                .header("X-Nonce", nonce)
                .header("X-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String signRaw(String method, String path, byte[] bodyBytes, String timestamp, String nonce) {
        try {
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            buf.write(method.toUpperCase().getBytes(StandardCharsets.UTF_8));
            buf.write('\n');
            buf.write(path.getBytes(StandardCharsets.UTF_8));
            buf.write('\n');
            buf.write(bodyBytes);
            buf.write('\n');
            buf.write(timestamp.getBytes(StandardCharsets.UTF_8));
            buf.write('\n');
            buf.write(nonce.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = buf.toByteArray();

            java.security.Signature signer = java.security.Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(signBytes);
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (Exception e) {
            throw new RuntimeException("RSA 签名失败", e);
        }
    }

    private String sign(String method, String path, String body, String timestamp, String nonce) {
        String signString = method.toUpperCase() + "\n" + path + "\n" + body + "\n" + timestamp + "\n" + nonce;
        return RsaSignUtil.sign(signString, privateKey);
    }

    private boolean isReady() {
        return privateKey != null;
    }
}
