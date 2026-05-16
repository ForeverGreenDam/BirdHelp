package com.greendam.birdhelp.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.utils.RsaSignUtil;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.properties.SignProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PublicKey;

/**
 * <p>
 * 内部接口签名校验过滤器，拦截 {@code /internal/**} 路径。
 * </p>
 *
 * <h3>验签流程</h3>
 * <ol>
 *   <li>缓存请求体（支持重复读取）</li>
 *   <li>校验时间戳是否在允许窗口内（防重放）</li>
 *   <li>拼接待签名字符串：{@code METHOD\nPATH\nBODY\nTIMESTAMP\nNONCE}</li>
 *   <li>使用 RSA 公钥验签</li>
 * </ol>
 *
 * <h3>请求头</h3>
 * <ul>
 *   <li>{@code X-Timestamp} — Unix 时间戳（毫秒）</li>
 *   <li>{@code X-Nonce} — 随机字符串，每次请求不同</li>
 *   <li>{@code X-Signature} — Base64 编码的 RSA-SHA256 签名</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Component
public class SignFilter implements Filter {

    @Resource
    private SignProperties signProperties;

    private PublicKey publicKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 内部接口路径前缀 */
    private static final String INTERNAL_PATH_PREFIX = "/internal";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, javax.servlet.ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 非 internal 路径直接放行
        if (!httpRequest.getRequestURI().startsWith(httpRequest.getContextPath() + INTERNAL_PATH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // 包装请求以缓存 Body
        CachedBodyHttpServletRequestWrapper wrappedRequest = new CachedBodyHttpServletRequestWrapper(httpRequest);

        try {
            verifySign(wrappedRequest);
        } catch (SignVerifyException e) {
            log.warn("内部接口验签失败: {}", e.getMessage());
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(httpResponse.getOutputStream(),
                    BaseResponse.error(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getMessage()));
            return;
        }

        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) {
        this.publicKey = RsaSignUtil.loadPublicKey(signProperties.getPublicKey());
        log.info("内部接口验签过滤器已初始化");
    }

    /**
     * 验证请求签名（字节级，直接对原始字节验签，避免 UTF-8 编解码差异）。
     */
    private void verifySign(CachedBodyHttpServletRequestWrapper request) throws SignVerifyException {
        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        String signature = request.getHeader("X-Signature");

        if (timestamp == null || nonce == null || signature == null) {
            throw new SignVerifyException("缺少签名请求头 (X-Timestamp, X-Nonce, X-Signature)");
        }
        if (timestamp.isBlank() || nonce.isBlank() || signature.isBlank()) {
            throw new SignVerifyException("签名请求头不能为空");
        }

        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new SignVerifyException("X-Timestamp 格式无效");
        }

        long now = System.currentTimeMillis();
        long window = signProperties.getTimestampWindow() * 1000;
        if (Math.abs(now - ts) > window) {
            throw new SignVerifyException("请求已过期或时间偏差过大: " + (now - ts) / 1000 + "s");
        }

        // 直接用原始字节构造签名字节串，避免 binary → UTF-8 String 编解码差异
        byte[] bodyBytes = request.getCachedBody();
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        try {
            buf.write(request.getMethod().toUpperCase().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            buf.write('\n');
            buf.write(request.getRequestURI().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            buf.write('\n');
            buf.write(bodyBytes);
            buf.write('\n');
            buf.write(timestamp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            buf.write('\n');
            buf.write(nonce.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new SignVerifyException("构造签名字节串失败");
        }

        if (!RsaSignUtil.verifyRaw(buf.toByteArray(), signature, publicKey)) {
            throw new SignVerifyException("签名不匹配");
        }
    }

    /**
     * 验签内部异常。
     */
    private static class SignVerifyException extends RuntimeException {
        SignVerifyException(String message) {
            super(message);
        }
    }
}
