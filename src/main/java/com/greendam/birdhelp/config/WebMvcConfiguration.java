package com.greendam.birdhelp.config;

import com.greendam.birdhelp.interceptor.JwtTokenAdminInterceptor;
import com.greendam.birdhelp.interceptor.JwtTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * <p>
 * Web MVC 配置类，注册拦截器、CORS 等 Web 层组件。
 * </p>
 *
 * <p>实现 {@link WebMvcConfigurer} 而非继承 {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport}，
 * 确保不会禁用 Spring Boot 的 MVC 自动配置（否则 Knife4j 静态资源无法加载）。</p>
 *
 * <h3>JWT 拦截器路径规则</h3>
 * <table border="1">
 *   <caption>拦截路径配置</caption>
 *   <tr><th>路径模式</th><th>是否需要 Token</th><th>说明</th></tr>
 *   <tr><td>{@code /user/login/**}</td><td align="center">否</td><td>登录接口（密码/短信/微信）</td></tr>
 *   <tr><td>{@code /user/register/**}</td><td align="center">否</td><td>注册接口（手机/邮箱）</td></tr>
 *   <tr><td>{@code /user/send-code}</td><td align="center">否</td><td>发送验证码</td></tr>
 *   <tr><td>{@code /user/reset-password}</td><td align="center">否</td><td>重置密码</td></tr>
 *   <tr><td>{@code /user/**}（其余）</td><td align="center">是</td><td>需携带有效 JWT</td></tr>
 * </table>
 *
 * @author ForeverGreenDam
 * @see JwtTokenInterceptor
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Resource
    private JwtTokenInterceptor jwtTokenUserInterceptor;

    @Resource
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * <p>注册 JWT 令牌校验拦截器并配置拦截范围。</p>
     *
     * <p>拦截所有 {@code /user/**} 路径，但排除登录、注册、发送验证码和重置密码等无需鉴权的接口。</p>
     *
     * @param registry Spring MVC 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**", "/quota/**", "/file/**", "/project/**", "/ppt/**", "/word/**", "/pdf/**", "/model/**", "/chat/**", "/member/**")
                .excludePathPatterns(
                        "/user/login/**",
                        "/user/register/**",
                        "/user/send-code",
                        "/user/reset-password",
                        "/pay/alipay/notify",
                        "/pay/alipay/return"
                );

        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");
    }

}
