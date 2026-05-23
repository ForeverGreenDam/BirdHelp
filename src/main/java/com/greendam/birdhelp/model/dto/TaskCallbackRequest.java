package com.greendam.birdhelp.model.dto;

import lombok.Data;

/**
 * Python AI 模块完成/失败回调请求体，对应协议 4.1 / 4.2。
 */
@Data
public class TaskCallbackRequest {

    private String taskId;
    private String callbackId;
    private Long userId;
    private Long projectId;
    private String status;
    private Long fileId;
    private String fileUrl;
    private String fileName;
    private Integer qaLowestScore;
    private Integer qaPassedCount;
    private Integer qaTotalCount;
    private Long generationTimeMs;
    private Integer errorCode;
    private String errorMessage;
}
