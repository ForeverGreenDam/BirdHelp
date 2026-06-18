package com.greendam.birdhelp.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.greendam.birdhelp.common.utils.AliMessageUtil;
import com.greendam.birdhelp.properties.AliMessageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AliMessageConfig {

    @Bean
    public Client getClient(AliMessageProperties aliMessageProperties) throws Exception {
        log.info("开始创建阿里短信服务客户端:{}", aliMessageProperties);
        Config config = new Config()
                .setAccessKeyId(aliMessageProperties.getAccessKeyId())
                .setAccessKeySecret(aliMessageProperties.getAccessKeySecret())
                .setEndpoint(aliMessageProperties.getEndpoint());
        return new Client(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public AliMessageUtil aliMessageUtil(AliMessageProperties aliMessageProperties) {
        log.info("开始创建阿里短信工具类对象:{}", aliMessageProperties);
        return new AliMessageUtil(
                aliMessageProperties.getEndpoint(),
                aliMessageProperties.getAccessKeyId(),
                aliMessageProperties.getAccessKeySecret(),
                aliMessageProperties.getSignName(),
                aliMessageProperties.getTemplateCode()
        );
    }

}
