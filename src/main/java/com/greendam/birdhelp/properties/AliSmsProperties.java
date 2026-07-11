package com.greendam.birdhelp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信服务配置属性类
 *
 * @author ForeverGreenDam
 */
@Component
@ConfigurationProperties(prefix = "alisms")
@Data
public class AliSmsProperties {

    /**
     * 阿里云 AccessKey ID
     */
    private String accessKeyId;

    /**
     * 阿里云 AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 短信签名名称
     */
    private String signName;

    /**
     * 短信模板 Code
     */
    private String templateCode;

}
