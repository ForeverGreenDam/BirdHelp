package com.greendam.birdhelp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "birdhelp.rabbitmq")
@Data
public class RabbitMQProperties {

    private String exchange = "birdhelp.doc.generation";
    private String queue = "birdhelp.doc.generation.tasks";
    private String dlx = "birdhelp.doc.generation.dlx";
    private String dlq = "birdhelp.doc.generation.dlq";
}
