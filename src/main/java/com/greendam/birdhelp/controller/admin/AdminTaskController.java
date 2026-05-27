package com.greendam.birdhelp.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.dto.DocGenerationMessage;
import com.greendam.birdhelp.model.vo.admin.AdminTaskVO;
import com.greendam.birdhelp.properties.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 管理员文档生成任务接口控制器，提供后台任务列表查询和失败任务重试功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>任务列表：从 Redis 中查询所有文档生成任务及其状态</li>
 *   <li>任务重试：重新发送失败的文档生成任务到 RabbitMQ 消息队列</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/task")
public class AdminTaskController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RabbitMQProperties mqProperties;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * <p>查询所有文档生成任务列表。</p>
     *
     * <p>遍历 Redis 中 {@code task:*} 前缀的键，组装每个任务的状态、用户 ID、文档类型、回调 ID 和错误信息。
     * 结果按任务 ID 降序排列。</p>
     *
     * @return 文档生成任务视图对象列表
     */
    @GetMapping("/list")
    public BaseResponse<List<AdminTaskVO>> list() {
        List<AdminTaskVO> tasks = new ArrayList<>();
        Set<String> keys = stringRedisTemplate.keys("task:*:status");
        if (keys != null) {
            for (String key : keys) {
                String taskId = key.replace("task:", "").replace(":status", "");
                String status = stringRedisTemplate.opsForValue().get(key);
                String userId = stringRedisTemplate.opsForValue().get("task:" + taskId + ":userId");
                String docType = stringRedisTemplate.opsForValue().get("task:" + taskId + ":docType");
                String callbackId = stringRedisTemplate.opsForValue().get("task:" + taskId + ":callbackId");
                String errorMessage = stringRedisTemplate.opsForValue().get("task:" + taskId + ":error");

                tasks.add(AdminTaskVO.builder()
                        .taskId(taskId)
                        .status(status)
                        .userId(userId)
                        .docType(docType)
                        .callbackId(callbackId)
                        .errorMessage(errorMessage)
                        .build());
            }
        }
        tasks.sort((a, b) -> b.getTaskId().compareTo(a.getTaskId()));
        return BaseResponse.success(tasks);
    }

    /**
     * <p>重试指定的失败文档生成任务。</p>
     *
     * <p>从 Redis 中读取原始任务的消息内容和路由键，重新生成新的任务 ID 并发送至 RabbitMQ 消息队列。
     * 原任务记录不会被删除。</p>
     *
     * @param taskId 待重试的原始任务 ID
     * @return 操作成功无数据返回；若原始消息已过期则返回 404 错误
     */
    @PostMapping("/{taskId}/retry")
    public BaseResponse<Void> retry(@PathVariable String taskId) {
        String messageJson = stringRedisTemplate.opsForValue().get("task:" + taskId + ":message");
        if (messageJson == null) {
            return BaseResponse.error(40400, "任务消息已过期或不存在");
        }
        String routingKey = stringRedisTemplate.opsForValue().get("task:" + taskId + ":routingKey");
        try {
            DocGenerationMessage msg = objectMapper.readValue(messageJson, DocGenerationMessage.class);
            if (routingKey == null) {
                routingKey = "doc.generate." + msg.getDocType();
            }

            String newTaskId = java.util.UUID.randomUUID().toString();
            msg.setTaskId(newTaskId);
            msg.setTimestamp(System.currentTimeMillis());

            String messageKey = "task:" + newTaskId + ":message";
            String routingKeyKey = "task:" + newTaskId + ":routingKey";
            stringRedisTemplate.opsForValue().set(messageKey, objectMapper.writeValueAsString(msg), java.time.Duration.ofHours(24));
            stringRedisTemplate.opsForValue().set(routingKeyKey, routingKey, java.time.Duration.ofHours(24));

            CorrelationData correlationData = new CorrelationData(newTaskId);
            rabbitTemplate.convertAndSend(mqProperties.getExchange(), routingKey, msg,
                    m -> {
                        m.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        m.getMessageProperties().setMessageId(newTaskId);
                        return m;
                    }, correlationData);

            log.info("管理员重试任务: oldTaskId={}, newTaskId={}, routingKey={}", taskId, newTaskId, routingKey);
            return BaseResponse.success();
        } catch (Exception e) {
            log.error("重试任务失败: taskId={}", taskId, e);
            return BaseResponse.error(50001, "重试失败: " + e.getMessage());
        }
    }
}
