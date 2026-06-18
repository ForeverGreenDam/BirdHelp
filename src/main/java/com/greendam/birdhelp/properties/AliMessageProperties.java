package com.greendam.birdhelp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信配置属性类
 * @author ForeverGreenDam
 */
@Component
@ConfigurationProperties(prefix = "alimessage")
@Data
public class AliMessageProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;

}
