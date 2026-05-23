package com.greendam.birdhelp.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 文档生成异步任务提交响应。
 */
@Data
@Builder
public class DocGenerateTaskVO {

    /**
     * 任务 ID（UUID v4），用于追踪和查询
     */
    private String taskId;
    /**
     * 状态：pending
     */
    private String status;
    /**
     * 业务流水 ID
     */
    private String callbackId;
}
