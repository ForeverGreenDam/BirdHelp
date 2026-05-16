package com.greendam.birdhelp.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.model.vo.PdfGenerateResultVO;
import com.greendam.birdhelp.model.vo.PptGenerateResultVO;
import com.greendam.birdhelp.model.vo.WordGenerateResultVO;
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
 * <p>
 * AI 模块调用客户端，对 /ai/** 接口发起带 RSA 签名的 HTTP 请求。
 * </p>
 *
 * <h3>调用方向</h3>
 * <p>Java 后端 → AI 模块（Python）。私钥由 Java 后端持有，公钥由 AI 模块持有。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Component
public class AiModuleCaller {

    @Resource
    private AiModuleProperties aiModuleProperties;

    @Resource
    private ObjectMapper objectMapper;

    private PrivateKey privateKey;
    private String baseUrl;
    private HttpClient httpClient;

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ==================== 业务方法 ====================

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
     *
     * @param javaFileId Java 端文件存储后返回的文件 ID，用于 AI 模块向量索引关联
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
     * 调用 AI 模块生成 PPT（同步，阻塞 20–60 秒）。
     *
     * @return PPT 生成结果（文件 ID、URL、文件名）
     * @throws BusinessException AI 模块返回错误或网络异常时抛出
     */
    public PptGenerateResultVO generatePpt(String userId, String projectId, String topic,
                                           String language, String style, Integer slideCount,
                                           String extraPrompt, java.util.List<String> materialIds,
                                           Boolean ragEnabled, String callbackId) {
        if (!isReady()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 模块未配置，无法生成 PPT");
        }
        try {
            java.util.Map<String, Object> bodyMap = new java.util.LinkedHashMap<>();
            bodyMap.put("user_id", userId);
            bodyMap.put("project_id", projectId);
            bodyMap.put("topic", topic);
            bodyMap.put("language", language != null ? language : "zh");
            bodyMap.put("style", style != null ? style : "academic");
            bodyMap.put("slide_count", slideCount != null ? slideCount : 10);
            bodyMap.put("extra_prompt", extraPrompt != null ? extraPrompt : "");
            bodyMap.put("material_ids", materialIds != null ? materialIds : java.util.List.of());
            bodyMap.put("rag_enabled", ragEnabled != null ? ragEnabled : false);
            bodyMap.put("callback_id", callbackId);

            String jsonBody = objectMapper.writeValueAsString(bodyMap);
            java.net.http.HttpResponse<String> resp = signedJsonRequest("POST", "/ai/ppt/generate", jsonBody);

            String respBody = resp.body();
            log.info("AI PPT 生成完成: status={}, body={}", resp.statusCode(), respBody);

            java.util.Map<String, Object> respMap = objectMapper.readValue(
                    respBody, new TypeReference<java.util.Map<String, Object>>() {
                    });

            // FastAPI 的 401/422 等错误使用 {"detail": ...} 格式
            int code = respMap.containsKey("code")
                    ? ((Number) respMap.get("code")).intValue()
                    : resp.statusCode();

            if (code != 0) {
                String message = (String) respMap.getOrDefault("message", "AI 模块未知错误");
                // 尝试从 FastAPI detail 字段提取错误消息
                if (respMap.containsKey("detail")) {
                    Object detail = respMap.get("detail");
                    if (detail instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> detailMap = (java.util.Map<String, Object>) detail;
                        message = (String) detailMap.getOrDefault("message", message);
                    } else {
                        message = String.valueOf(detail);
                    }
                }
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PPT 生成失败: " + message);
            }

            // AI 模块的 data 内包装了 Java FileInternalController 返回的 BaseResponse<FileRecordVO>
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> javaResp = (java.util.Map<String, Object>) respMap.get("data");
            if (javaResp == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PPT 生成返回数据为空");
            }
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> fileRecord = (java.util.Map<String, Object>) javaResp.get("data");
            if (fileRecord == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PPT 生成返回文件信息为空");
            }

            return PptGenerateResultVO.builder()
                    .fileId(Long.valueOf(String.valueOf(fileRecord.get("id"))))
                    .fileUrl((String) fileRecord.get("fileUrl"))
                    .fileName((String) fileRecord.get("fileName"))
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI PPT 生成失败: userId={}, projectId={}, topic={}", userId, projectId, topic, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PPT 生成请求失败，请稍后重试");
        }
    }

    /**
     * 调用 AI 模块生成 Word（同步，阻塞 20–60 秒）。
     *
     * @return Word 生成结果（文件 ID、URL、文件名）
     * @throws BusinessException AI 模块返回错误或网络异常时抛出
     */
    public WordGenerateResultVO generateWord(String userId, String projectId, String topic,
                                             String language, String docType, Integer wordCount,
                                             String extraPrompt, java.util.List<String> materialIds,
                                             Boolean ragEnabled, String callbackId) {
        if (!isReady()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 模块未配置，无法生成 Word");
        }
        try {
            java.util.Map<String, Object> bodyMap = new java.util.LinkedHashMap<>();
            bodyMap.put("user_id", userId);
            bodyMap.put("project_id", projectId);
            bodyMap.put("topic", topic);
            bodyMap.put("language", language != null ? language : "zh");
            bodyMap.put("doc_type", docType != null ? docType : "essay");
            bodyMap.put("word_count", wordCount != null ? wordCount : 2000);
            bodyMap.put("extra_prompt", extraPrompt != null ? extraPrompt : "");
            bodyMap.put("material_ids", materialIds != null ? materialIds : java.util.List.of());
            bodyMap.put("rag_enabled", ragEnabled != null ? ragEnabled : false);
            bodyMap.put("callback_id", callbackId);

            String jsonBody = objectMapper.writeValueAsString(bodyMap);
            java.net.http.HttpResponse<String> resp = signedJsonRequest("POST", "/ai/word/generate", jsonBody);

            String respBody = resp.body();
            log.info("AI Word 生成完成: status={}, body={}", resp.statusCode(), respBody);

            java.util.Map<String, Object> respMap = objectMapper.readValue(
                    respBody, new TypeReference<java.util.Map<String, Object>>() {
                    });

            int code = respMap.containsKey("code")
                    ? ((Number) respMap.get("code")).intValue()
                    : resp.statusCode();

            if (code != 0) {
                String message = (String) respMap.getOrDefault("message", "AI 模块未知错误");
                if (respMap.containsKey("detail")) {
                    Object detail = respMap.get("detail");
                    if (detail instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> detailMap = (java.util.Map<String, Object>) detail;
                        message = (String) detailMap.getOrDefault("message", message);
                    } else {
                        message = String.valueOf(detail);
                    }
                }
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Word 生成失败: " + message);
            }

            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> javaResp = (java.util.Map<String, Object>) respMap.get("data");
            if (javaResp == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Word 生成返回数据为空");
            }
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> fileRecord = (java.util.Map<String, Object>) javaResp.get("data");
            if (fileRecord == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Word 生成返回文件信息为空");
            }

            return WordGenerateResultVO.builder()
                    .fileId(Long.valueOf(String.valueOf(fileRecord.get("id"))))
                    .fileUrl((String) fileRecord.get("fileUrl"))
                    .fileName((String) fileRecord.get("fileName"))
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI Word 生成失败: userId={}, projectId={}, topic={}", userId, projectId, topic, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Word 生成请求失败，请稍后重试");
        }
    }

    /**
     * 调用 AI 模块生成 PDF（同步，阻塞 20–90 秒，含 LibreOffice 转换）。
     *
     * @return PDF 生成结果（文件 ID、URL、文件名）
     * @throws BusinessException AI 模块返回错误或网络异常时抛出
     */
    public PdfGenerateResultVO generatePdf(String userId, String projectId, String topic,
                                           String language, String docType,
                                           String extraPrompt, java.util.List<String> materialIds,
                                           Boolean ragEnabled, String callbackId) {
        if (!isReady()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 模块未配置，无法生成 PDF");
        }
        try {
            java.util.Map<String, Object> bodyMap = new java.util.LinkedHashMap<>();
            bodyMap.put("user_id", userId);
            bodyMap.put("project_id", projectId);
            bodyMap.put("topic", topic);
            bodyMap.put("language", language != null ? language : "zh");
            bodyMap.put("doc_type", docType != null ? docType : "report");
            bodyMap.put("extra_prompt", extraPrompt != null ? extraPrompt : "");
            bodyMap.put("material_ids", materialIds != null ? materialIds : java.util.List.of());
            bodyMap.put("rag_enabled", ragEnabled != null ? ragEnabled : false);
            bodyMap.put("callback_id", callbackId);

            String jsonBody = objectMapper.writeValueAsString(bodyMap);
            java.net.http.HttpResponse<String> resp = signedJsonRequest("POST", "/ai/pdf/generate", jsonBody);

            String respBody = resp.body();
            log.info("AI PDF 生成完成: status={}, body={}", resp.statusCode(), respBody);

            java.util.Map<String, Object> respMap = objectMapper.readValue(
                    respBody, new TypeReference<java.util.Map<String, Object>>() {
                    });

            int code = respMap.containsKey("code")
                    ? ((Number) respMap.get("code")).intValue()
                    : resp.statusCode();

            if (code != 0) {
                String message = (String) respMap.getOrDefault("message", "AI 模块未知错误");
                if (respMap.containsKey("detail")) {
                    Object detail = respMap.get("detail");
                    if (detail instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> detailMap = (java.util.Map<String, Object>) detail;
                        message = (String) detailMap.getOrDefault("message", message);
                    } else {
                        message = String.valueOf(detail);
                    }
                }
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PDF 生成失败: " + message);
            }

            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> javaResp = (java.util.Map<String, Object>) respMap.get("data");
            if (javaResp == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PDF 生成返回数据为空");
            }
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> fileRecord = (java.util.Map<String, Object>) javaResp.get("data");
            if (fileRecord == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PDF 生成返回文件信息为空");
            }

            return PdfGenerateResultVO.builder()
                    .fileId(Long.valueOf(String.valueOf(fileRecord.get("id"))))
                    .fileUrl((String) fileRecord.get("fileUrl"))
                    .fileName((String) fileRecord.get("fileName"))
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI PDF 生成失败: userId={}, projectId={}, topic={}", userId, projectId, topic, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PDF 生成请求失败，请稍后重试");
        }
    }

    // ==================== 底层 HTTP 签名方法 ====================

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

    /**
     * 对原始字节 body 签名（用于 multipart，避免 binary → UTF-8 String 编解码差异）。
     */
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
