package com.greendam.birdhelp.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.common.utils.JwtUtil;
import com.greendam.birdhelp.constant.JwtClaimsConstant;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * JWT 令牌校验拦截器，在请求到达 Controller 之前对受保护路径进行鉴权。
 * </p>
 *
 * <h3>拦截流程</h3>
 * <ol>
 *   <li>判断当前拦截目标是否为 Controller 方法，非动态资源直接放行</li>
 *   <li>从请求头 {@code token} 中获取 JWT 令牌</li>
 *   <li>解析令牌，提取 {@code id} 声明作为当前用户 ID</li>
 *   <li>将用户 ID 存入 {@link BaseContext} 的 ThreadLocal 中，供后续处理使用</li>
 *   <li>请求结束后在 {@link #afterCompletion} 中清理 ThreadLocal，防止内存泄漏</li>
 * </ol>
 *
 * <h3>鉴权失败处理</h3>
 * <p>令牌缺失、过期或签名不匹配时，返回 HTTP 401 状态码并拦截请求。</p>
 *
 * @author ForeverGreenDam
 * @see com.greendam.birdhelp.config.WebMvcConfiguration
 */
@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * <p>Controller 方法调用前执行，完成 JWT 校验和用户身份注入。</p>
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  目标处理器（非 {@link HandlerMethod} 时直接放行）
     * @return {@code true} 放行，{@code false} 拦截（返回 401）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("进入JWT令牌校验拦截器...");
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getUserTokenName());

        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：{}", userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            log.error("JWT校验失败: {}", ex.getMessage(), ex);
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            BaseResponse<?> errorResponse = BaseResponse.error(
                    ErrorCode.NOT_LOGIN_ERROR.getCode(),
                    "登录已过期，请重新登录");
            try {
                objectMapper.writeValue(response.getOutputStream(), errorResponse);
            } catch (Exception writeEx) {
                log.error("写入JWT错误响应失败", writeEx);
            }
            return false;
        }
    }

    /**
     * <p>请求完成后清理 ThreadLocal，防止内存泄漏。</p>
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  目标处理器
     * @param ex       处理器执行中抛出的异常（可为 {@code null}）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
