package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志实体，映射 {@code operation_log} 表。
 * </p>
 *
 * <h3>用途</h3>
 * <p>记录管理员在后台系统中的所有操作行为，用于审计和追溯。</p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 日志主键 ID，自增</li>
 *   <li>{@code adminId} - 执行操作的管理员 ID</li>
 *   <li>{@code adminName} - 执行操作的管理员名称</li>
 *   <li>{@code action} - 操作动作，例如 {@code "CREATE"}、{@code "UPDATE"}、{@code "DELETE"}</li>
 *   <li>{@code targetType} - 操作目标类型，例如 {@code "user"}、{@code "api_key"}、{@code "announcement"}</li>
 *   <li>{@code targetId} - 操作目标的主键 ID</li>
 *   <li>{@code detail} - 操作详情描述</li>
 *   <li>{@code createTime} - 日志记录时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@TableName(value = "operation_log")
public class OperationLog {

    /**
     * 日志主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
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
     * 日志记录时间。
     */
    private LocalDateTime createTime;
}
