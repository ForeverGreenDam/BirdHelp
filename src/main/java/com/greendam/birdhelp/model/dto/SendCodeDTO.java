package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * <p>
 * 发送验证码请求体。
 * </p>
 *
 * <h3>type 取值说明</h3>
 * <ul>
 *   <li>{@code register} — 注册场景</li>
 *   <li>{@code login} — 登录场景</li>
 *   <li>{@code reset} — 重置密码场景</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class SendCodeDTO {

    /**
     * 手机号或邮箱地址，验证码的接收目标。
     */
    @NotBlank(message = "手机号或邮箱不能为空")
    private String target;

    /**
     * 验证码使用场景类型，可选值：{@code register}、{@code login}、{@code reset}。
     */
    @NotBlank(message = "验证码类型不能为空")
    @Pattern(regexp = "register|login|reset", message = "验证码类型: register/login/reset")
    private String type;
}
