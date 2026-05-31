package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 对话修改会话表实体类，映射数据表 {@code chat_session}，继承 {@link BaseEntity} 获得审计字段。
 * </p>
 *
 * <h3>会话与文件的绑定关系</h3>
 * <ul>
 *   <li>{@code originalFileId}：修改的起点文件（用户点击修改按钮时的文件）</li>
 *   <li>{@code currentFileId}：当前最新版本文件（每次修改后更新）</li>
 *   <li>对话窗口隶属于 {@code sessionId}，不隶属于具体文件</li>
 * </ul>
 *
 * <h3>审计字段（继承自 {@link BaseEntity}）</h3>
 * <p>{@code createTime} / {@code createBy} / {@code updateTime} / {@code updateBy} / {@code delFlag}
 * 由 {@code MyMetaObjectHandler} 自动填充。</p>
 *
 * <h3>版本链</h3>
 * <p>每次修改生成新文件，通过 {@code file_record.version_of} 形成单向链表。
 * 列表只展示链尾文件，但用户可通过会话回顾任意历史版本。</p>
 *
 * @author ForeverGreenDam
 * @see com.greendam.birdhelp.model.entity.FileRecord#getVersionOf()
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "chat_session")
@Data
public class ChatSession extends BaseEntity {

    /**
     * 主键 ID，数据库自增。
     */
    @TableId
    private Long id;

    /**
     * 会话 ID（UUID v4），对外暴露给前端和 Python AI 模块。
     */
    private String sessionId;

    /**
     * 用户 ID，关联 {@code sys_user.id}。
     */
    private Long userId;

    /**
     * 项目 ID，关联 {@code project.id}。
     */
    private Long projectId;

    /**
     * 修改的起点文件 ID（用户点击修改按钮时的文件），关联 {@code file_record.id}。
     */
    private Long originalFileId;

    /**
     * 当前最新版本文件 ID（每次修改后更新），关联 {@code file_record.id}。
     * 可为 {@code null}（尚未产生修改版本时）。
     */
    private Long currentFileId;

    /**
     * 文档类型：{@code "ppt"} / {@code "word"} / {@code "pdf"}。
     */
    private String docType;

    /**
     * 会话标题（取自原始文件名，创建时自动填入，支持后续重命名）。
     */
    private String title;

    /**
     * 消息总数（冗余字段，避免每次 COUNT 查询）。
     */
    private Integer messageCount;
}
