package com.greendam.birdhelp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * RabbitMQ 文档生成消息体，对应协议 v1.0。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocGenerationMessage {

    /**
     * 协议版本
     */
    private String version;
    /**
     * 任务唯一 ID（UUID v4）
     */
    private String taskId;
    /**
     * Java 端原始请求 ID
     */
    private String callbackId;
    /**
     * 文档类型：ppt | word | pdf
     */
    private String docType;
    /**
     * 用户 ID
     */
    private String userId;
    /**
     * 项目 ID
     */
    private String projectId;
    /**
     * 文档主题
     */
    private String topic;
    /**
     * 语言：zh | en
     */
    private String language;
    /**
     * 补充指令
     */
    private String extraPrompt;
    /**
     * RAG 素材 ID 列表
     */
    private List<String> materialIds;
    /**
     * 是否启用 RAG
     */
    private Boolean ragEnabled;
    /**
     * 设计风格
     */
    private String style;
    /**
     * 是否自动搜索配图
     */
    private Boolean enableImages;

    // ---- PPT 专属 ----
    /**
     * 幻灯片页数
     */
    private Integer slideCount;

    // ---- Word / PDF 专属 ----
    /**
     * 文档子类型
     */
    private String docSubtype;
    /**
     * 目标字数（Word）
     */
    private Integer wordCount;

    /**
     * 消息生产时间戳（毫秒）
     */
    private Long timestamp;
}
