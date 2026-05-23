package com.greendam.birdhelp.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * 文档生成任务状态查询响应，覆盖 pending / processing / completed / failed 四种状态。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskStatusVO {

    /**
     * 任务 ID
     */
    private String taskId;
    /**
     * pending | processing | completed | failed
     */
    private String status;

    // ---- processing 时 ----
    /**
     * 当前阶段
     */
    private String stage;
    /**
     * 进度 0-100
     */
    private Integer progress;
    /**
     * 可读状态描述
     */
    private String message;

    // ---- completed 时 ----
    /**
     * 文件 ID
     */
    private Long fileId;
    /**
     * 文件访问 URL
     */
    private String fileUrl;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * QA 最低评分
     */
    private Integer qaLowestScore;
    /**
     * QA 通过数
     */
    private Integer qaPassedCount;
    /**
     * QA 总数
     */
    private Integer qaTotalCount;

    // ---- failed 时 ----
    /**
     * 错误码
     */
    private Integer errorCode;
    /**
     * 错误描述
     */
    private String errorMessage;
}
