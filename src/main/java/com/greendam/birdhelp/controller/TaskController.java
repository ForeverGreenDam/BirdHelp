package com.greendam.birdhelp.controller;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.vo.TaskStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 文档生成任务状态查询接口，供前端轮询。
 */
@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {

    private static final String TASK_PREFIX = "task:";
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询任务状态。
     *
     * <p>前端提交生成任务拿到 taskId 后，轮询本接口获取最新状态。</p>
     * <ul>
     *   <li>pending — 任务已提交，尚未开始处理</li>
     *   <li>processing — AI 模块正在生成（含阶段和进度）</li>
     *   <li>completed — 生成完成（含 fileId、fileUrl、fileName）</li>
     *   <li>failed — 生成失败（含 errorCode、errorMessage）</li>
     * </ul>
     */
    @GetMapping("/{taskId}")
    public BaseResponse<TaskStatusVO> status(@PathVariable String taskId) {
        String key = TASK_PREFIX + taskId;

        String status = stringRedisTemplate.opsForValue().get(key + ":status");

        if ("completed".equals(status)) {
            return BaseResponse.success(TaskStatusVO.builder()
                    .taskId(taskId)
                    .status("completed")
                    .fileId(getLong(key + ":fileId"))
                    .fileUrl(stringRedisTemplate.opsForValue().get(key + ":fileUrl"))
                    .fileName(stringRedisTemplate.opsForValue().get(key + ":fileName"))
                    .qaLowestScore(getInt(key + ":qaLowestScore"))
                    .qaPassedCount(getInt(key + ":qaPassedCount"))
                    .qaTotalCount(getInt(key + ":qaTotalCount"))
                    .build());
        }

        if ("failed".equals(status)) {
            return BaseResponse.success(TaskStatusVO.builder()
                    .taskId(taskId)
                    .status("failed")
                    .errorCode(getInt(key + ":errorCode"))
                    .errorMessage(stringRedisTemplate.opsForValue().get(key + ":errorMessage"))
                    .build());
        }

        String stage = stringRedisTemplate.opsForValue().get(key + ":stage");
        String progress = stringRedisTemplate.opsForValue().get(key + ":progress");
        if (stage != null || progress != null) {
            return BaseResponse.success(TaskStatusVO.builder()
                    .taskId(taskId)
                    .status("processing")
                    .stage(stage)
                    .progress(progress != null ? Integer.parseInt(progress) : null)
                    .message(stringRedisTemplate.opsForValue().get(key + ":message"))
                    .build());
        }

        return BaseResponse.success(TaskStatusVO.builder()
                .taskId(taskId)
                .status("pending")
                .build());
    }

    private Long getLong(String key) {
        String val = stringRedisTemplate.opsForValue().get(key);
        if (val == null || val.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getInt(String key) {
        String val = stringRedisTemplate.opsForValue().get(key);
        if (val == null || val.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
