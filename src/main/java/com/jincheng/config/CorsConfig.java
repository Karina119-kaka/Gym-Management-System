package com.jincheng.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有接口跨域
        registry.addMapping("/**")
                // 允许前端所有来源（生产环境可替换为具体域名）
                .allowedOriginPatterns("*")
                // 允许所有请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许携带Cookie
                .allowCredentials(true)
                // 预检请求有效期3600秒
                .maxAge(3600);
    }
}