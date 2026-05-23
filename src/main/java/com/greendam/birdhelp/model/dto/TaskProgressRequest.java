package com.greendam.birdhelp.model.dto;

import lombok.Data;

/**
 * Python AI 模块进度通知请求体，对应协议 4.4。
 */
@Data
public class TaskProgressRequest {

    private String taskId;
    private String callbackId;
    private String status;
    private String stage;
    private Integer progress;
    private String message;
}
