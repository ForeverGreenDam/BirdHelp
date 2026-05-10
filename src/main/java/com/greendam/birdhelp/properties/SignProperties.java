package com.greendam.birdhelp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 内部接口加签验签配置。
 * </p>
 *
 * <p>Java 后端使用 {@code public-key} 校验 AI 模块发来的请求签名，
 * 私钥由 AI 模块持有，用于生成签名。</p>
 *
 * @author ForeverGreenDam
 */
@Component
@ConfigurationProperties(prefix = "internal-api")
@Data
public class SignProperties {

    /** RSA 公钥（Base64 编码的 X.509 DER 格式），用于验签 */
    private String publicKey;

    /** 时间戳有效期窗口（秒），默认 300 秒（5 分钟） */
    private long timestampWindow = 300;
}
