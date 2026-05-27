package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 管理员登录 DTO，用于接收管理员的登录请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code account} - 管理员账号，不能为空</li>
 *   <li>{@code password} - 管理员密码，不能为空</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class AdminLoginDTO {

    /**
     * 管理员账号，不能为空。
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 管理员密码，不能为空。
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
