package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 对话消息明细表实体类，映射数据表 {@code chat_message}。
 * </p>
 *
 * <h3>存储策略</h3>
 * <p>消息拆为独立行而非 JSON 字段存储，好处是：</p>
 * <ul>
 *   <li>单条消息用 {@code TEXT}（64KB）足够，无需 {@code MEDIUMTEXT}</li>
 *   <li>可按时间排序 / 分页</li>
 *   <li>无需担心 JSON 容量上限</li>
 * </ul>
 *
 * <h3>文件关联</h3>
 * <p>{@code fileId} 为可选字段，assistant 消息可关联该轮产出的文件版本。
 * 前端可通过历史消息中的 {@code fileId} 回顾该轮生成的文档。</p>
 *
 * @author ForeverGreenDam
 */
@TableName(value = "chat_message")
@Data
public class ChatMessage implements Serializable {

    /**
     * 主键 ID，数据库自增。
     */
    @TableId
    private Long id;

    /**
     * 关联的会话 ID，对应 {@code chat_session.session_id}。
     */
    private String sessionId;

    /**
     * 消息角色：{@code "user"}（用户消息）或 {@code "assistant"}（AI 回复）。
     */
    private String role;

    /**
     * 消息内容（用户消息文本或 AI 回复文本）。
     */
    private String content;

    /**
     * 关联的文件 ID（可选）。
     * assistant 消息可关联该轮产出的文件版本，用于前端回顾历史文档。
     */
    private Long fileId;

    /**
     * 消息创建时间。
     */
    private LocalDateTime createTime;
}
