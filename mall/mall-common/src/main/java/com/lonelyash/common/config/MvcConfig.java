package com.lonelyash.common.config;

import com.lonelyash.common.interceptor.UserInfoInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(DispatcherServlet.class)
public class MvcConfig implements WebMvcConfigurer {
    @Override
    //利用 Spring 的拦截器机制增强 MVC 流程，避免侵入业务代码
    //通过实现 WebMvcConfigurer 接口，动态注册自定义拦截器 UserInfoInterceptor，
    // 用于在请求处理前后执行特定逻辑（如用户身份校验、日志记录等）
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor());
    }

}
