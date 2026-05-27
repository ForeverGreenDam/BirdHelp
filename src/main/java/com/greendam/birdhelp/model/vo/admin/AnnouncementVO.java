package com.greendam.birdhelp.model.vo.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 公告视图对象，用于展示公告的完整信息。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 公告 ID</li>
 *   <li>{@code title} - 公告标题</li>
 *   <li>{@code content} - 公告正文内容</li>
 *   <li>{@code status} - 发布状态：{@code 0} - 草稿，{@code 1} - 已发布</li>
 *   <li>{@code publishTime} - 发布时间</li>
 *   <li>{@code createTime} - 创建时间</li>
 *   <li>{@code updateTime} - 更新时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class AnnouncementVO {
    /**
     * 公告 ID。
     */
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
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
