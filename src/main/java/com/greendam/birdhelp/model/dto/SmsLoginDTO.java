package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * <p>
 * 短信验证码登录请求体。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class SmsLoginDTO {

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

}
