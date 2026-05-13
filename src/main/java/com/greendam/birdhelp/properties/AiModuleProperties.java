package com.greendam.birdhelp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 * AI 模块调用配置。Java 后端使用 {@code private-key} 对发往 AI 模块的请求签名，
 * 公钥由 AI 模块持有，用于验签。
 * </p>
 *
 * <h3>与 internal-api 密钥对的关系</h3>
 * <p>这是独立的一对密钥，与 AI 模块调用 Java 内部接口（{@code /api/internal/**}）使用的密钥对
 * 是两套不同的密钥，不可混用。</p>
 *
 * @author ForeverGreenDam
 */
@Component
@ConfigurationProperties(prefix = "ai-module")
@Data
public class AiModuleProperties {

    /**
     * AI 模块基础 URL，如 http://localhost:8000
     */
    private String baseUrl;

    /**
     * RSA 私钥（Base64 编码的 PKCS#8 DER 格式），用于对发往 AI 模块的请求签名。
     */
    private String privateKey;
}
