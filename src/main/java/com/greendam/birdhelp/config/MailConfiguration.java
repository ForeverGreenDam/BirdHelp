package com.greendam.birdhelp.config;

import com.greendam.birdhelp.common.utils.MailUtil;
import com.greendam.birdhelp.properties.MailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件配置类，用于创建 MailUtil 实例。
 *
 * @author ForeverGreenDam
 */
@Configuration
@Slf4j
public class MailConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MailUtil mailUtil(JavaMailSender mailSender, MailProperties mailProperties) {
        log.info("开始创建邮件工具类实例");
        return new MailUtil(mailSender, mailProperties);
    }
}
