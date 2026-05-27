package com.greendam.birdhelp.model.vo.admin;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 管理员任务视图对象，用于展示异步文档生成任务的执行状态。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code taskId} - 异步任务 ID</li>
 *   <li>{@code status} - 任务状态，例如 {@code "PENDING"}、{@code "RUNNING"}、{@code "SUCCESS"}、{@code "FAILED"}</li>
 *   <li>{@code docType} - 文档类型，例如 {@code "word"}、{@code "pdf"}、{@code "ppt"}</li>
 *   <li>{@code userId} - 发起任务的用户 ID</li>
 *   <li>{@code callbackId} - 回调标识 ID</li>
 *   <li>{@code errorMessage} - 任务失败时的错误信息</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class AdminTaskVO {
    /**
     * 异步任务 ID。
     */
    private String taskId;
    /**
     * 任务状态，例如 {@code "PENDING"}、{@code "RUNNING"}、{@code "SUCCESS"}、{@code "FAILED"}。
     */
    private String status;
    /**
     * 文档类型，例如 {@code "word"}、{@code "pdf"}、{@code "ppt"}。
     */
    private String docType;
    /**
     * 发起任务的用户 ID。
     */
    private String userId;
    /**
     * 回调标识 ID。
     */
    private String callbackId;
    /**
     * 任务失败时的错误信息。
     */
    private String errorMessage;
}
