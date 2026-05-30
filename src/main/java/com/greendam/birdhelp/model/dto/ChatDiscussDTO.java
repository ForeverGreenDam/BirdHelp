package com.greendam.birdhelp.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 对话讨论/仅问答请求体，前端 → Java → Python 代理。
 * </p>
 *
 * <h3>与 {@link ChatModifyDTO} 的区别</h3>
 * <p>此接口不重建文件，仅返回 AI 文本回复。不需要 {@code regenerateFile} 字段。</p>
 *
 * @author ForeverGreenDam
 */
@Data
public class ChatDiscussDTO {

    /**
     * 会话 ID（UUID v4）
     */
    private String sessionId;

    /**
     * 当前编辑的源文件 ID
     */
    private String fileId;

    /**
     * 文档类型：ppt / word / pdf
     */
    private String docType;

    /**
     * 用户当前的消息
     */
    private String message;

    /**
     * 所属项目 ID
     */
    private Long projectId;

    /**
     * 历史消息列表
     */
    private List<Map<String, String>> history;
}
