package com.greendam.birdhelp.model.vo.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 管理员额度变更日志视图对象，用于展示用户额度的调整记录。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 日志 ID</li>
 *   <li>{@code userId} - 被调整额度的用户 ID</li>
 *   <li>{@code username} - 被调整额度的用户名</li>
 *   <li>{@code changeType} - 变更类型：{@code 1} - 管理员调整，{@code 2} - 消费扣减，{@code 3} - 每日重置</li>
 *   <li>{@code relatedId} - 关联业务 ID（如任务 ID 或订单 ID）</li>
 *   <li>{@code createTime} - 变更时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class AdminQuotaLogVO {
    /**
     * 日志 ID。
     */
    private Long id;
    /**
     * 被调整额度的用户 ID。
     */
    private Long userId;
    /**
     * 被调整额度的用户名。
     */
    private String username;
    /**
     * 变更类型：{@code 1} - 管理员调整，{@code 2} - 消费扣减，{@code 3} - 每日重置。
     */
    private Integer changeType;
    /**
     * 关联业务 ID（如任务 ID 或订单 ID）。
     */
    private String relatedId;
    /**
     * 变更时间。
     */
    private LocalDateTime createTime;
}
