package com.greendam.birdhelp.config;

import com.greendam.birdhelp.properties.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 拓扑声明与 RabbitTemplate 配置。
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String RK_PPT = "doc.generate.ppt";

    // ==================== 路由键常量 ====================
    public static final String RK_WORD = "doc.generate.word";
    public static final String RK_PDF = "doc.generate.pdf";
    public static final String RK_DLQ = "doc.generate.dlq";
    @Resource
    private RabbitMQProperties mqProperties;

    // ==================== DLX / DLQ ====================

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(mqProperties.getDlx(), true, false);
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(mqProperties.getDlq()).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(dlxExchange()).with(RK_DLQ);
    }

    // ==================== 主交换机 ====================

    @Bean
    public TopicExchange docGenerationExchange() {
        return new TopicExchange(mqProperties.getExchange(), true, false);
    }

    // ==================== 主队列（含死信参数） ====================

    @Bean
    public Queue docGenerationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", mqProperties.getDlx());
        args.put("x-dead-letter-routing-key", RK_DLQ);
        args.put("x-message-ttl", 600_000);
        args.put("x-max-priority", 10);
        return QueueBuilder.durable(mqProperties.getQueue())
                .withArguments(args)
                .build();
    }

    // ==================== 绑定 ====================

    @Bean
    public Binding pptBinding() {
        return BindingBuilder.bind(docGenerationQueue()).to(docGenerationExchange()).with(RK_PPT);
    }

    @Bean
    public Binding wordBinding() {
        return BindingBuilder.bind(docGenerationQueue()).to(docGenerationExchange()).with(RK_WORD);
    }

    @Bean
    public Binding pdfBinding() {
        return BindingBuilder.bind(docGenerationQueue()).to(docGenerationExchange()).with(RK_PDF);
    }

    // ==================== RabbitTemplate ====================

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息已确认: id={}", correlationData != null ? correlationData.getId() : "null");
            } else {
                log.error("消息发送失败(nack): id={}, cause={}",
                        correlationData != null ? correlationData.getId() : "null", cause);
            }
        });
        template.setReturnsCallback(returned -> {
            log.error("消息被退回: replyCode={}, replyText={}, exchange={}, routingKey={}",
                    returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
        });
        return template;
    }
}
