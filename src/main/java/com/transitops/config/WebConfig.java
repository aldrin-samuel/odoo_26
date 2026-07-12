package com.transitops.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RbacInterceptor rbacInterceptor;

    public WebConfig(RbacInterceptor rbacInterceptor) {
        this.rbacInterceptor = rbacInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Intercept all /api/** routes
        registry.addInterceptor(rbacInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
    }
}