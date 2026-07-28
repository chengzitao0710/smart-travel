package com.smarttravel.common.config;

import com.smarttravel.common.utils.LoginInterceptor;
import com.smarttravel.common.utils.RefreshTokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/traveler/code",
                        "/traveler/login/code",
                        "/traveler/login/password",
                        "/scenic/{id}",
                        "/scenic/of/**",
                        "/scenic/poi",
                        "/scenic/nearby",
                        "/scenic/hot",
                        "/scenic/top",
                        "/scenic/list",
                        "/scenic-type/**",
                        "/upload/**",
                        "/search/**",
                        "/doc.html",
                        "/v3/api-docs/**",
                        "/webjars/**"
                ).order(1);
        registry.addInterceptor(new RefreshTokenInterceptor())
                .addPathPatterns("/**").order(0);
    }
}