package com.greendam.birdhelp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件配置属性类
 *
 * @author ForeverGreenDam
 */
@Component
@ConfigurationProperties(prefix = "mail")
@Data
public class MailProperties {

    /**
     * 发件人邮箱
     */
    private String from;

    /**
     * 发件人显示名称
     */
    private String fromName = "BirdHelp";
}
