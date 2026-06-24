package com.djnd.cinema_java_spring.security;

import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GuestAwareJwtDecoder implements JwtDecoder {
    final JwtDecoder jwtDecoder;
    final HttpServletRequest request;
    final List<String> publishAntPatterns;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();
            String currentPath = request.getRequestURI();
            boolean isPublishEndPoint = publishAntPatterns.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, currentPath));
            if (isPublishEndPoint) {
                return Jwt.withTokenValue("anonymous_token")
                        .header("alg", "none")
                        .subject("anonymousUser")
                        .claim("scope", "")
                        .expiresAt(Instant.MAX)
                        .issuedAt(Instant.now())
                        .build();
            }
            throw e;
        }
    }
}
