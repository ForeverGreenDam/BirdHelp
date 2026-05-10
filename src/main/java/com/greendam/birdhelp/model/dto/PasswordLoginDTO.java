package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 密码登录请求体。
 * </p>
 *
 * <p>{@code account} 字段支持手机号、邮箱或用户名三种登录凭证。</p>
 *
 * @author ForeverGreenDam
 */
@Data
public class PasswordLoginDTO {

    /**
     * 登录账号，支持手机号、邮箱或用户名。
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 登录密码（明文传输，服务端 BCrypt 校验）。
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
