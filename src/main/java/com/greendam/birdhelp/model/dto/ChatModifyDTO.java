package com.greendam.birdhelp.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 对话修改文档请求体，前端 → Java → Python 代理。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code sessionId}：会话 ID（UUID v4），同一修改对话的唯一标识</li>
 *   <li>{@code fileId}：当前编辑的源文件 ID</li>
 *   <li>{@code docType}：文档类型（ppt / word / pdf）</li>
 *   <li>{@code message}：用户当前的修改指令</li>
 *   <li>{@code projectId}：所属项目 ID</li>
 *   <li>{@code history}：历史消息列表，每项含 {@code role} 和 {@code content}</li>
 *   <li>{@code regenerateFile}：是否重建文件，默认 {@code true}</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class ChatModifyDTO {

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
     * 用户当前的修改指令
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

    /**
     * 是否重建文件，默认 true
     */
    private Boolean regenerateFile;
}
