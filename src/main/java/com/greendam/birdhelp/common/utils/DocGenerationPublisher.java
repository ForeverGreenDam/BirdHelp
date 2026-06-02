package com.greendam.birdhelp.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.config.RabbitMQConfig;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.model.dto.DocGenerationMessage;
import com.greendam.birdhelp.model.vo.DocGenerateTaskVO;
import com.greendam.birdhelp.properties.RabbitMQProperties;
import com.greendam.birdhelp.service.admin.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文档生成任务发布器，将生成请求转为 RabbitMQ 消息异步投递。
 */
@Slf4j
@Component
public class DocGenerationPublisher {

    private static final String PROTOCOL_VERSION = "1.0";
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQProperties mqProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ApiKeyService apiKeyService;

    /**
     * 发布 PPT 生成任务。
     */
    public DocGenerateTaskVO publishPpt(String userId, String projectId, String topic,
                                        String language, String style, Integer slideCount,
                                        String extraPrompt, Boolean enableImages,
                                        List<String> materialIds, Boolean ragEnabled,
                                        String callbackId, String modelName) {
        Map<String, String> creds = apiKeyService.resolveCredentials(modelName);
        DocGenerationMessage msg = DocGenerationMessage.builder()
                .version(PROTOCOL_VERSION)
                .taskId(UUID.randomUUID().toString())
                .callbackId(callbackId)
                .docType("ppt")
                .userId(userId)
                .projectId(projectId)
                .topic(topic)
                .language(language != null ? language : "zh")
                .style(style != null ? style : "academic")
                .slideCount(slideCount != null ? slideCount : 10)
                .extraPrompt(extraPrompt)
                .enableImages(enableImages != null ? enableImages : true)
                .materialIds(materialIds)
                .ragEnabled(ragEnabled != null ? ragEnabled : false)
                .apiKey(creds.get("apiKey"))
                .baseUrl(creds.get("baseUrl"))
                .modelName(creds.get("modelName"))
                .timestamp(System.currentTimeMillis())
                .build();
        send(msg, RabbitMQConfig.RK_PPT, 8);
        return DocGenerateTaskVO.builder()
                .taskId(msg.getTaskId())
                .status("pending")
                .callbackId(callbackId)
                .build();
    }

    /**
     * 发布 Word 生成任务。
     */
    public DocGenerateTaskVO publishWord(String userId, String projectId, String topic,
                                         String language, String docType, Integer wordCount,
                                         String style, String extraPrompt, Boolean enableImages,
                                         List<String> materialIds, Boolean ragEnabled,
                                         String callbackId, String modelName) {
        Map<String, String> creds = apiKeyService.resolveCredentials(modelName);
        DocGenerationMessage msg = DocGenerationMessage.builder()
                .version(PROTOCOL_VERSION)
                .taskId(UUID.randomUUID().toString())
                .callbackId(callbackId)
                .docType("word")
                .userId(userId)
                .projectId(projectId)
                .topic(topic)
                .language(language != null ? language : "zh")
                .docSubtype(docType != null ? docType : "essay")
                .wordCount(wordCount != null ? wordCount : 2000)
                .style(style != null ? style : "academic")
                .extraPrompt(extraPrompt)
                .enableImages(enableImages != null ? enableImages : true)
                .materialIds(materialIds)
                .ragEnabled(ragEnabled != null ? ragEnabled : false)
                .apiKey(creds.get("apiKey"))
                .baseUrl(creds.get("baseUrl"))
                .modelName(creds.get("modelName"))
                .timestamp(System.currentTimeMillis())
                .build();
        send(msg, RabbitMQConfig.RK_WORD, 3);
        return DocGenerateTaskVO.builder()
                .taskId(msg.getTaskId())
                .status("pending")
                .callbackId(callbackId)
                .build();
    }

    /**
     * 发布 PDF 生成任务。
     */
    public DocGenerateTaskVO publishPdf(String userId, String projectId, String topic,
                                        String language, String docType,
                                        String style, String extraPrompt, Boolean enableImages,
                                        List<String> materialIds, Boolean ragEnabled,
                                        String callbackId, String modelName) {
        Map<String, String> creds = apiKeyService.resolveCredentials(modelName);
        DocGenerationMessage msg = DocGenerationMessage.builder()
                .version(PROTOCOL_VERSION)
                .taskId(UUID.randomUUID().toString())
                .callbackId(callbackId)
                .docType("pdf")
                .userId(userId)
                .projectId(projectId)
                .topic(topic)
                .language(language != null ? language : "zh")
                .docSubtype(docType != null ? docType : "report")
                .style(style != null ? style : "academic")
                .extraPrompt(extraPrompt)
                .enableImages(enableImages != null ? enableImages : true)
                .materialIds(materialIds)
                .ragEnabled(ragEnabled != null ? ragEnabled : false)
                .apiKey(creds.get("apiKey"))
                .baseUrl(creds.get("baseUrl"))
                .modelName(creds.get("modelName"))
                .timestamp(System.currentTimeMillis())
                .build();
        send(msg, RabbitMQConfig.RK_PDF, 3);
        return DocGenerateTaskVO.builder()
                .taskId(msg.getTaskId())
                .status("pending")
                .callbackId(callbackId)
                .build();
    }

    /**
     * 重发失败的任务。从 Redis 读取原始消息，清除旧状态后重新投递。
     */
    public void resend(String taskId) {
        String messageKey = "task:" + taskId + ":message";
        String routingKeyKey = "task:" + taskId + ":routingKey";

        String messageJson = stringRedisTemplate.opsForValue().get(messageKey);
        String routingKey = stringRedisTemplate.opsForValue().get(routingKeyKey);

        if (messageJson == null || routingKey == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务消息已过期或不存在");
        }

        DocGenerationMessage msg;
        try {
            msg = objectMapper.readValue(messageJson, DocGenerationMessage.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "任务消息解析失败");
        }

        String key = "task:" + taskId;
        stringRedisTemplate.delete(key + ":status");
        stringRedisTemplate.delete(key + ":errorCode");
        stringRedisTemplate.delete(key + ":errorMessage");
        stringRedisTemplate.delete(key + ":stage");
        stringRedisTemplate.delete(key + ":progress");
        stringRedisTemplate.delete(key + ":fileId");
        stringRedisTemplate.delete(key + ":fileUrl");
        stringRedisTemplate.delete(key + ":fileName");
        stringRedisTemplate.delete(key + ":qaLowestScore");
        stringRedisTemplate.delete(key + ":qaPassedCount");
        stringRedisTemplate.delete(key + ":qaTotalCount");

        int priority = switch (msg.getDocType()) {
            case "ppt" -> 8;
            default -> 3;
        };
        send(msg, routingKey, priority);
    }

    // ==================== 底层发送 ====================

    private void send(DocGenerationMessage msg, String routingKey, int priority) {
        try {
            String taskKey = "task:" + msg.getTaskId();
            String messageKey = taskKey + ":message";
            String routingKeyKey = taskKey + ":routingKey";

            stringRedisTemplate.opsForValue().set(messageKey, objectMapper.writeValueAsString(msg), Duration.ofHours(24));
            stringRedisTemplate.opsForValue().set(routingKeyKey, routingKey, Duration.ofHours(24));

            CorrelationData correlationData = new CorrelationData(msg.getTaskId());

            rabbitTemplate.convertAndSend(mqProperties.getExchange(), routingKey, msg,
                    m -> {
                        m.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        m.getMessageProperties().setMessageId(msg.getTaskId());
                        m.getMessageProperties().setPriority(priority);
                        return m;
                    }, correlationData);

            log.info("文档生成消息已发送: taskId={}, docType={}, routingKey={}, callbackId={}",
                    msg.getTaskId(), msg.getDocType(), routingKey, msg.getCallbackId());
        } catch (Exception e) {
            log.error("文档生成消息发送失败: taskId={}, docType={}", msg.getTaskId(), msg.getDocType(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        }
    }
}
