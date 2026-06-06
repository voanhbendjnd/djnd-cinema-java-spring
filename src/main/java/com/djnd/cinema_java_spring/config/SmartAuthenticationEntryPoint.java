package com.djnd.cinema_java_spring.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * smart Authentication Entry Point để handle 401 responses
 *
 * request đến protected API với invalid token -> 401 redirect
 * info
 *
 * not: if does endpoints exists đã được
 * whitelist trong securityConfig
 * (handle enpoint not allow and if not login)
 * kích hoạt khi jwt hết hạn hoặc không có
 */
@Component
public class SmartAuthenticationEntryPoint implements
        AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorMessage = """
                {
                "error": "Unauthorized",
                "message": "(session expired) Please login to access this resource",
                "code": "UNAUTHORIZED",
                "publicApi": false,
                "redirect": true,
                "redirectUrl": "/login"
                }
                """;
        response.getWriter().write(errorMessage);
    }
}