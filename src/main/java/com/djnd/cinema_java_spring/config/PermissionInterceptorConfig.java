package com.djnd.cinema_java_spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.djnd.cinema_java_spring.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PermissionInterceptorConfig implements WebMvcConfigurer {
    final UserRepository userRepository;

    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor(userRepository);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                // "/api/v1/permissions/data"
        };
        String[] securePatterns = { "/api/v1/users/**",
                "/api/v1/roles/**",
                "/api/v1/permissions/**" };
        registry.addInterceptor(getPermissionInterceptor()).excludePathPatterns(whiteList)
                .addPathPatterns(securePatterns);
        // registry.addInterceptor(getPermissionInterceptor()).addPathPatterns("/admin/**");
    }
}