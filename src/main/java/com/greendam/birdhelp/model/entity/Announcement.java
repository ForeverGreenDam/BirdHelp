package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 * 公告实体，映射 {@code announcement} 表。
 * </p>
 *
 * <h3>用途</h3>
 * <p>存储系统公告信息，用于向用户展示平台通知、更新动态等内容。</p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 公告主键 ID</li>
 *   <li>{@code title} - 公告标题</li>
 *   <li>{@code content} - 公告正文内容</li>
 *   <li>{@code status} - 发布状态：{@code 0} - 草稿，{@code 1} - 已发布</li>
 *   <li>{@code publishTime} - 发布时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 * @see com.greendam.birdhelp.model.entity.BaseEntity
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "announcement")
@Data
public class Announcement extends BaseEntity {

    /**
     * 公告主键 ID。
     */
    @TableId
    private Long id;

    /**
     * 公告标题。
     */
    private String title;

    /**
     * 公告正文内容。
     */
    private String content;

    /**
     * 发布状态：{@code 0} - 草稿，{@code 1} - 已发布。
     */
    private Integer status;

    /**
     * 发布时间。
     */
    private LocalDateTime publishTime;
}
