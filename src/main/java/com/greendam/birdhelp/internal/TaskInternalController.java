package com.greendam.birdhelp.internal;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.dto.TaskCallbackRequest;
import com.greendam.birdhelp.model.dto.TaskProgressRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 文档生成任务回调接口，供 Python AI 模块调用（RSA 签名校验）。
 */
@Slf4j
@RestController
@RequestMapping("/internal")
public class TaskInternalController {

    private static final String TASK_PREFIX = "task:";
    private static final long TASK_TTL_HOURS = 24;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 接收任务完成/失败回调（协议 4.1 / 4.2）。
     */
    @PostMapping("/task/callback")
    public BaseResponse<Void> callback(@RequestBody TaskCallbackRequest req) {
        String taskId = req.getTaskId();
        String status = req.getStatus();
        log.info("收到任务回调: taskId={}, status={}, fileId={}, errorCode={}, generationTimeMs={}",
                taskId, status, req.getFileId(), req.getErrorCode(), req.getGenerationTimeMs());

        String key = TASK_PREFIX + taskId;
        if ("completed".equals(status)) {
            stringRedisTemplate.opsForValue().set(key + ":status", "completed", TASK_TTL_HOURS, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(key + ":fileId", String.valueOf(req.getFileId()), TASK_TTL_HOURS, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(key + ":fileUrl", req.getFileUrl() != null ? req.getFileUrl() : "", TASK_TTL_HOURS, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(key + ":fileName", req.getFileName() != null ? req.getFileName() : "", TASK_TTL_HOURS, TimeUnit.HOURS);
            if (req.getQaLowestScore() != null) {
                stringRedisTemplate.opsForValue().set(key + ":qaLowestScore", String.valueOf(req.getQaLowestScore()), TASK_TTL_HOURS, TimeUnit.HOURS);
            }
            if (req.getQaPassedCount() != null) {
                stringRedisTemplate.opsForValue().set(key + ":qaPassedCount", String.valueOf(req.getQaPassedCount()), TASK_TTL_HOURS, TimeUnit.HOURS);
            }
            if (req.getQaTotalCount() != null) {
                stringRedisTemplate.opsForValue().set(key + ":qaTotalCount", String.valueOf(req.getQaTotalCount()), TASK_TTL_HOURS, TimeUnit.HOURS);
            }
            log.info("任务完成: taskId={}, fileId={}, fileUrl={}", taskId, req.getFileId(), req.getFileUrl());
        } else {
            stringRedisTemplate.opsForValue().set(key + ":status", "failed", TASK_TTL_HOURS, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(key + ":errorCode", String.valueOf(req.getErrorCode()), TASK_TTL_HOURS, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(key + ":errorMessage", req.getErrorMessage() != null ? req.getErrorMessage() : "", TASK_TTL_HOURS, TimeUnit.HOURS);
            log.warn("任务失败: taskId={}, errorCode={}, errorMessage={}", taskId, req.getErrorCode(), req.getErrorMessage());
        }

        return BaseResponse.success();
    }

    /**
     * 接收任务进度通知（协议 4.4，可选实现）。
     */
    @PostMapping("/task/progress")
    public BaseResponse<Void> progress(@RequestBody TaskProgressRequest req) {
        log.info("任务进度: taskId={}, stage={}, progress={}%, message={}",
                req.getTaskId(), req.getStage(), req.getProgress(), req.getMessage());

        String key = TASK_PREFIX + req.getTaskId();
        stringRedisTemplate.opsForValue().set(key + ":stage", req.getStage(), TASK_TTL_HOURS, TimeUnit.HOURS);
        stringRedisTemplate.opsForValue().set(key + ":progress", String.valueOf(req.getProgress()), TASK_TTL_HOURS, TimeUnit.HOURS);
        if (req.getMessage() != null) {
            stringRedisTemplate.opsForValue().set(key + ":message", req.getMessage(), TASK_TTL_HOURS, TimeUnit.HOURS);
        }

        return BaseResponse.success();
    }
}
