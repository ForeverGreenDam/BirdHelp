package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 邮箱注册请求体。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class EmailRegisterDTO {

    /**
     * 邮箱地址，须符合标准邮箱格式。
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 邮箱收到的 6 位验证码。
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 登录用户名，长度 2-50 位，全局唯一。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度为2-50位")
    private String username;

    /**
     * 登录密码，长度 6-100 位，BCrypt 加密存储。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度为6-100位")
    private String password;

    /**
     * 用户昵称（显示名称），最长 50 位。
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长50位")
    private String nickname;
}
