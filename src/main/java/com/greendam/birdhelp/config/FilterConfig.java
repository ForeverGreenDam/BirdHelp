package com.greendam.birdhelp.config;

import com.greendam.birdhelp.common.filter.SignFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Filter 配置类，统一注册所有自定义过滤器。
 * </p>
 *
 * <h3>过滤器执行顺序</h3>
 * <p>{@code setOrder(0)} 确保签名过滤器在其他过滤器之前执行，尽早拦截非法请求。</p>
 *
 * @author ForeverGreenDam
 */
@Configuration
public class FilterConfig {

    /**
     * 注册内部接口签名校验过滤器。
     *
     * @param signFilter 签名过滤器实例
     * @return 过滤器注册 Bean
     */
    @Bean
    public FilterRegistrationBean<SignFilter> signFilterRegistration(SignFilter signFilter) {
        FilterRegistrationBean<SignFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(signFilter);
        registration.addUrlPatterns("/internal/*", "/internal/quota/*", "/internal/task/*");
        registration.setOrder(0);
        return registration;
    }
}
