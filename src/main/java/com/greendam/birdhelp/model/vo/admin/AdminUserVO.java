package com.greendam.birdhelp.model.vo.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员用户视图对象，用于展示管理员视角下的用户基本信息。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 用户 ID</li>
 *   <li>{@code username} - 用户名</li>
 *   <li>{@code nickname} - 用户昵称</li>
 *   <li>{@code avatar} - 头像 URL</li>
 *   <li>{@code phone} - 手机号</li>
 *   <li>{@code email} - 电子邮箱</li>
 *   <li>{@code sex} - 性别：{@code 0} - 未知，{@code 1} - 男，{@code 2} - 女</li>
 *   <li>{@code birthday} - 出生日期</li>
 *   <li>{@code userType} - 用户类型：{@code 1} - 普通学生，{@code 2} - 管理员</li>
 *   <li>{@code status} - 账号状态：{@code 0} - 禁用，{@code 1} - 正常</li>
 *   <li>{@code createTime} - 创建时间</li>
 *   <li>{@code updateTime} - 更新时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class AdminUserVO {
    /**
     * 用户 ID。
     */
    private Long id;
    /**
     * 用户名。
     */
    private String username;
    /**
     * 用户昵称。
     */
    private String nickname;
    /**
     * 头像 URL。
     */
    private String avatar;
    /**
     * 手机号。
     */
    private String phone;
    /**
     * 电子邮箱。
     */
    private String email;
    /**
     * 性别：{@code 0} - 未知，{@code 1} - 男，{@code 2} - 女。
     */
    private Integer sex;
    /**
     * 出生日期。
     */
    private LocalDate birthday;
    /**
     * 用户类型：{@code 1} - 普通学生，{@code 2} - 管理员。
     */
    private Integer userType;
    /**
     * 账号状态：{@code 0} - 禁用，{@code 1} - 正常。
     */
    private Integer status;
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
