package com.djnd.cinema_java_spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.djnd.cinema_java_spring.repository.PermissionRepository;
import com.djnd.cinema_java_spring.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PermissionInterceptorConfig implements WebMvcConfigurer {
    final PermissionRepository permissionRepository;
    final UserRepository userRepository;

    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor(permissionRepository, userRepository);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                // "/api/v1/admin/users/**"
        };
        String[] securePatterns = {
                "/api/v1/admin/**",
                // "/api/admin/v1/roles/**",
                // "/api/admin/v1/permissions/**",
        };
        registry.addInterceptor(getPermissionInterceptor()).excludePathPatterns(whiteList)
                .addPathPatterns(securePatterns);
        // registry.addInterceptor(getPermissionInterceptor()).addPathPatterns("/admin/**");
    }
}