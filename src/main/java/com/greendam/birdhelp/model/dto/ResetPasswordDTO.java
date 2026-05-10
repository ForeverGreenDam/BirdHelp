package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 重置密码请求体（忘记密码场景）。
 * </p>
 *
 * <p>通过手机号或邮箱配合验证码来验证身份后重置密码，无需登录。</p>
 *
 * @author ForeverGreenDam
 */
@Data
public class ResetPasswordDTO {

    /**
     * 手机号或邮箱地址，用于定位目标用户。
     */
    @NotBlank(message = "手机号或邮箱不能为空")
    private String account;

    /**
     * 发送到目标手机号或邮箱的验证码。
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 新密码，长度 6-100 位，BCrypt 加密后替换原密码。
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度为6-100位")
    private String newPassword;
}
