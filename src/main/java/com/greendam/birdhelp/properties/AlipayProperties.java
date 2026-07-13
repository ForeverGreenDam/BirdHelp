package com.greendam.birdhelp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 支付宝支付配置类，映射 {@code application.yml} 中 {@code alipay.*} 配置项。
 * </p>
 *
 * <h3>配置说明</h3>
 * <ul>
 *   <li>{@code appId} - 支付宝应用ID</li>
 *   <li>{@code privateKey} - 应用私钥（RSA2）</li>
 *   <li>{@code alipayPublicKey} - 支付宝公钥</li>
 *   <li>{@code gatewayUrl} - 支付宝网关地址</li>
 *   <li>{@code notifyUrl} - 异步回调地址</li>
 *   <li>{@code returnUrl} - 同步跳转地址</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Component
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayProperties {

    /**
     * 支付宝应用ID。
     */
    private String appId;

    /**
     * 应用私钥（RSA2 签名使用）。
     */
    private String privateKey;

    /**
     * 支付宝公钥（用于验签）。
     */
    private String alipayPublicKey;

    /**
     * 支付宝网关地址，默认 {@code https://openapi.alipay.com/gateway.do}。
     */
    private String gatewayUrl = "https://openapi.alipay.com/gateway.do";

    /**
     * 异步回调地址，支付宝服务端通知支付结果。
     */
    private String notifyUrl;

    /**
     * 同步跳转地址，用户支付完成后浏览器跳转。
     */
    private String returnUrl;

    /**
     * 签名类型，默认 {@code RSA2}。
     */
    private String signType = "RSA2";

    /**
     * 字符编码，默认 {@code UTF-8}。
     */
    private String charset = "UTF-8";
}
