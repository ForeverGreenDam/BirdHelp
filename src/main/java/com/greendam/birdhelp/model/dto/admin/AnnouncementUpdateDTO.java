package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * <p>
 * 公告更新 DTO，用于接收管理员修改已有公告的请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 要更新的公告 ID，不能为空</li>
 *   <li>{@code title} - 公告标题</li>
 *   <li>{@code content} - 公告正文内容</li>
 *   <li>{@code status} - 发布状态：{@code 0} - 草稿，{@code 1} - 已发布</li>
 *   <li>{@code publishTime} - 发布时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class AnnouncementUpdateDTO {
    /**
     * 要更新的公告 ID，不能为空。
     */
    @NotNull(message = "ID不能为空")
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
