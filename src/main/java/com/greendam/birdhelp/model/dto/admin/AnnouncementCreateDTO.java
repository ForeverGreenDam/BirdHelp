package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 公告创建 DTO，用于接收管理员新增公告的请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code title} - 公告标题，不能为空</li>
 *   <li>{@code content} - 公告正文内容，不能为空</li>
 *   <li>{@code status} - 发布状态：{@code 0} - 草稿，{@code 1} - 已发布</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class AnnouncementCreateDTO {
    /**
     * 公告标题，不能为空。
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 公告正文内容，不能为空。
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 发布状态：{@code 0} - 草稿，{@code 1} - 已发布。
     */
    private Integer status;
}
