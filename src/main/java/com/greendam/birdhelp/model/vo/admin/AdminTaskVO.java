package com.greendam.birdhelp.model.vo.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 管理员任务视图对象，展示异步文档生成任务的状态及结果。
 * </p>
 *
 * <h3>按状态返回不同字段</h3>
 * <ul>
 *   <li>{@code pending} — 仅返回 {@code taskId} 和 {@code status}</li>
 *   <li>{@code processing} — 额外返回 {@code stage}、{@code progress}、{@code message}</li>
 *   <li>{@code completed} — 额外返回 {@code fileId}、{@code fileName}、{@code fileUrl}、{@code qaLowestScore}、{@code qaTotalCount}</li>
 *   <li>{@code failed} — 额外返回 {@code errorCode}、{@code errorMessage}</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminTaskVO {

    private String taskId;
    private String status;

    // ---- processing ----
    private String stage;
    private Integer progress;
    private String message;

    // ---- completed ----
    private Long fileId;
    private String fileUrl;
    private String fileName;
    private Integer qaLowestScore;
    private Integer qaTotalCount;

    // ---- failed ----
    private Integer errorCode;
    private String errorMessage;
}
