package com.greendam.birdhelp.controller.admin;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.utils.DocGenerationPublisher;
import com.greendam.birdhelp.model.vo.admin.AdminTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 管理员文档生成任务接口控制器，提供任务列表查询和任务详情功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>任务列表：从 Redis 中查询所有文档生成任务及其状态</li>
 *   <li>任务详情：查看指定任务的完整信息</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/task")
public class AdminTaskController {

    private static final String TASK_PREFIX = "task:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DocGenerationPublisher docGenerationPublisher;

    /**
     * <p>查询所有文档生成任务列表。</p>
     *
     * <p>遍历 Redis 中 {@code task:*:status} 前缀的键，按状态组装每个任务的对应字段。
     * 结果按任务 ID 降序排列。</p>
     *
     * @return 文档生成任务视图对象列表
     */
    @GetMapping("/list")
    public BaseResponse<List<AdminTaskVO>> list() {
        List<AdminTaskVO> tasks = new ArrayList<>();
        Set<String> keys = stringRedisTemplate.keys(TASK_PREFIX + "*:status");
        if (keys != null) {
            for (String key : keys) {
                String taskId = key.replace(TASK_PREFIX, "").replace(":status", "");
                tasks.add(buildTaskVO(taskId));
            }
        }
        tasks.sort((a, b) -> b.getTaskId().compareTo(a.getTaskId()));
        return BaseResponse.success(tasks);
    }

    /**
     * <p>查询指定任务的详细信息。</p>
     *
     * @param taskId 任务 ID
     * @return 任务详情视图对象
     */
    @GetMapping("/{taskId}")
    public BaseResponse<AdminTaskVO> detail(@PathVariable String taskId) {
        AdminTaskVO vo = buildTaskVO(taskId);
        if (vo.getStatus() == null) {
            return BaseResponse.error(40400, "任务不存在或已过期");
        }
        return BaseResponse.success(vo);
    }

    /**
     * <p>重试失败的任务。从 Redis 读取原始消息并重新投递到 RabbitMQ。</p>
     *
     * @param taskId 任务 ID
     * @return 重试结果
     */
    @PostMapping("/{taskId}/retry")
    public BaseResponse<Void> retry(@PathVariable String taskId) {
        String status = stringRedisTemplate.opsForValue().get(TASK_PREFIX + taskId + ":status");
        if (status == null) {
            return BaseResponse.error(40400, "任务不存在或已过期");
        }
        if (!"failed".equals(status)) {
            return BaseResponse.error(40000, "只能重试失败的任务，当前状态: " + status);
        }
        docGenerationPublisher.resend(taskId);
        return BaseResponse.success();
    }

    /**
     * <p>根据任务 ID 从 Redis 组装完整的任务视图对象。</p>
     */
    private AdminTaskVO buildTaskVO(String taskId) {
        String key = TASK_PREFIX + taskId;
        String status = stringRedisTemplate.opsForValue().get(key + ":status");

        if ("completed".equals(status)) {
            return AdminTaskVO.builder()
                    .taskId(taskId)
                    .status("completed")
                    .fileId(getLong(key + ":fileId"))
                    .fileUrl(stringRedisTemplate.opsForValue().get(key + ":fileUrl"))
                    .fileName(stringRedisTemplate.opsForValue().get(key + ":fileName"))
                    .qaLowestScore(getInt(key + ":qaLowestScore"))
                    .qaTotalCount(getInt(key + ":qaTotalCount"))
                    .build();
        }

        if ("failed".equals(status)) {
            return AdminTaskVO.builder()
                    .taskId(taskId)
                    .status("failed")
                    .errorCode(getInt(key + ":errorCode"))
                    .errorMessage(stringRedisTemplate.opsForValue().get(key + ":errorMessage"))
                    .build();
        }

        String stage = stringRedisTemplate.opsForValue().get(key + ":stage");
        String progress = stringRedisTemplate.opsForValue().get(key + ":progress");
        if (stage != null || progress != null) {
            return AdminTaskVO.builder()
                    .taskId(taskId)
                    .status("processing")
                    .stage(stage)
                    .progress(progress != null ? Integer.parseInt(progress) : null)
                    .message(stringRedisTemplate.opsForValue().get(key + ":message"))
                    .build();
        }

        return AdminTaskVO.builder()
                .taskId(taskId)
                .status("pending")
                .build();
    }

    private Long getLong(String key) {
        String val = stringRedisTemplate.opsForValue().get(key);
        if (val == null || val.isEmpty()) return null;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getInt(String key) {
        String val = stringRedisTemplate.opsForValue().get(key);
        if (val == null || val.isEmpty()) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
