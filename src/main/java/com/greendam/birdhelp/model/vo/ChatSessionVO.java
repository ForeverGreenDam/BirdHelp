package com.greendam.birdhelp.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>
 * 会话列表项视图对象，用于左侧栏会话列表（{@code GET /chat/sessions}）。
 * </p>
 *
 * <h3>列表展示</h3>
 * <p>前端左侧栏每个标签显示：标题 + 文档类型图标 + 消息数 + 最后更新时间。
 * 不包含消息内容（消息仅在点击后通过 {@code GET /chat/session/{id}} 加载）。</p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO {

    /**
     * 会话 ID（UUID v4），后续对话和加载历史时使用
     */
    private String sessionId;

    /**
     * 会话标题（取自原始文件名）
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
     * 消息总数
     */
    private Integer messageCount;

    /**
     * 最后一条消息的文本预览（截取前 50 字），用于标签上的副标题
     */
    private String lastMessagePreview;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}
