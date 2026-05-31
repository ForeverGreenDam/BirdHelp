package com.greendam.birdhelp.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 会话详情视图对象，用于点击会话标签后加载完整历史消息（{@code GET /chat/session/{sessionId}}）。
 * </p>
 *
 * <h3>包含内容</h3>
 * <ul>
 *   <li>会话元信息（sessionId / title / originalFileId / currentFileId / docType）</li>
 *   <li>完整消息列表（按时间正序）</li>
 *   <li>每一条 assistant 消息上的 {@code fileId} 指向该轮产出的文件版本</li>
 * </ul>
 *
 * <h3>版本时间线</h3>
 * <p>前端遍历 messages，收集所有非空 {@code fileId} 的 assistant 消息，
 * 即可构建版本时间线（v1 → v2 → ... → 当前）。</p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDetailVO {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 所属项目 ID
     */
    private Long projectId;

    /**
     * 原始文件 ID（修改起点）
     */
    private Long originalFileId;

    /**
     * 当前最新版本文件 ID
     */
    private Long currentFileId;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 文档类型：ppt / word / pdf
     */
    private String docType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 完整消息列表（按时间正序），每项含 role / content / fileId / createTime
     */
    private List<Map<String, Object>> messages;
}
