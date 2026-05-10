package com.greendam.birdhelp.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 登录成功响应视图。
 * </p>
 *
 * <p>包含 JWT 令牌和用户基本信息。客户端应持久化 Token，后续请求通过 {@code token} 请求头携带。</p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class LoginVO {

    /**
     * JWT 访问令牌（HS256 签名），后续鉴权请求须在请求头 {@code token} 中携带。
     */
    private String token;

    /**
     * 当前登录用户的基本信息。
     */
    private UserInfoVO userInfo;
}
