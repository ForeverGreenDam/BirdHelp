package com.greendam.birdhelp.model.vo.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志视图对象，用于展示管理员后台操作记录。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 日志 ID</li>
 *   <li>{@code adminId} - 执行操作的管理员 ID</li>
 *   <li>{@code adminName} - 执行操作的管理员名称</li>
 *   <li>{@code action} - 操作动作，例如 {@code "CREATE"}、{@code "UPDATE"}、{@code "DELETE"}</li>
 *   <li>{@code targetType} - 操作目标类型，例如 {@code "user"}、{@code "api_key"}、{@code "announcement"}</li>
 *   <li>{@code targetId} - 操作目标的主键 ID</li>
 *   <li>{@code detail} - 操作详情描述</li>
 *   <li>{@code createTime} - 操作时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class OperationLogVO {
    /**
     * 日志 ID。
     */
    private Long id;
    /**
     * 执行操作的管理员 ID。
     */
    private Long adminId;
    /**
     * 执行操作的管理员名称。
     */
    private String adminName;
    /**
     * 操作动作，例如 {@code "CREATE"}、{@code "UPDATE"}、{@code "DELETE"}。
     */
    private String action;
    /**
     * 操作目标类型，例如 {@code "user"}、{@code "api_key"}、{@code "announcement"}。
     */
    private String targetType;
    /**
     * 操作目标的主键 ID。
     */
    private String targetId;
    /**
     * 操作详情描述。
     */
    private String detail;
    /**
     * 操作时间。
     */
    private LocalDateTime createTime;
}
