package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 修改密码请求体（已登录状态）。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class UpdatePasswordDTO {

    /**
     * 当前使用的旧密码，用于身份验证。
     */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 要设置的新密码，长度 6-100 位，BCrypt 加密后存储。
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度为6-100位")
    private String newPassword;
}
