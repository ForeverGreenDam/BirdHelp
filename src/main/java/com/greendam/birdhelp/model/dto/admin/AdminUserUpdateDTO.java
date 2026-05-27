package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

/**
 * <p>
 * 管理员更新用户信息 DTO，用于接收管理员修改用户基本资料的请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code nickname} - 用户昵称</li>
 *   <li>{@code phone} - 手机号</li>
 *   <li>{@code email} - 电子邮箱</li>
 *   <li>{@code sex} - 性别：{@code 0} - 未知，{@code 1} - 男，{@code 2} - 女</li>
 *   <li>{@code birthday} - 出生日期（字符串形式）</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class AdminUserUpdateDTO {
    /**
     * 用户昵称。
     */
    private String nickname;
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
     * 出生日期（字符串形式）。
     */
    private String birthday;
}
