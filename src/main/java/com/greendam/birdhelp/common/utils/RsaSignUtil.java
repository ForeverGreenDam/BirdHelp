package com.greendam.birdhelp.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <p>
 * RSA 加签验签工具类，使用 SHA256withRSA 算法。
 * </p>
 *
 * <h3>密钥格式</h3>
 * <ul>
 *   <li>公钥：X.509 DER 格式，Base64 编码</li>
 *   <li>私钥：PKCS#8 DER 格式，Base64 编码</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
public class RsaSignUtil {

    /** 签名算法 */
    private static final String ALGORITHM = "SHA256withRSA";
    /** 密钥算法 */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 从 Base64 编码的 X.509 DER 公钥字符串中加载公钥。
     *
     * @param base64PublicKey Base64 编码的公钥
     * @return 公钥对象
     */
    public static PublicKey loadPublicKey(String base64PublicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("加载 RSA 公钥失败", e);
        }
    }

    /**
     * 从 Base64 编码的 PKCS#8 DER 私钥字符串中加载私钥。
     *
     * @param base64PrivateKey Base64 编码的私钥
     * @return 私钥对象
     */
    public static PrivateKey loadPrivateKey(String base64PrivateKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("加载 RSA 私钥失败", e);
        }
    }

    /**
     * 使用私钥对数据进行签名。
     *
     * @param data       待签名的原始数据
     * @param privateKey 私钥
     * @return Base64 编码的签名
     */
    public static String sign(String data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA 签名失败", e);
        }
    }

    /**
     * 使用公钥验证签名。
     *
     * @param data      原始数据
     * @param sign      Base64 编码的签名
     * @param publicKey 公钥
     * @return {@code true} 验签通过，{@code false} 验签失败
     */
    public static boolean verify(String data, String sign, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            return false;
        }
    }
}
