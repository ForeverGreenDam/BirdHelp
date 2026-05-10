package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * <p>
 * 手机号注册请求体。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class PhoneRegisterDTO {

    /**
     * 手机号，须符合中国大陆手机号格式（1 开头，第二位 3-9，共 11 位）。
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 手机收到的 6 位短信验证码。
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
