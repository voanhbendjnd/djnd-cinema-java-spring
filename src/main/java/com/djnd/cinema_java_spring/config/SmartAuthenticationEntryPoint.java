package com.djnd.cinema_java_spring.config;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
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
 * not: Public APIs (books, categories) đã được whitelist trong
 * securityConfig
 * nên sẽ không bao giờ đến đây
 * kích hoạt khi jwt hết hạn hoặc không có
 */
@Component
public class SmartAuthenticationEntryPoint implements
        AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (authException instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String errorPassword = """
                    {
                    "error": "Bad Request",
                    "message": "Login or password incorrect!",
                    "code": "BAD_CREDENTIALS",
                    "redirect": false
                    }
                    """;
            response.getWriter().write(errorPassword);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String errorMessage = """
                {
                "error": "Unauthorized",
                "message": "(session expired) Please login to access this resource",
                "code": "UNAUTHORIZED",
                "redirect": true,
                "redirectUrl": "/login"
                }
                """;
        response.getWriter().write(errorMessage);
    }
}