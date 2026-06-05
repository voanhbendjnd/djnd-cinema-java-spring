package com.djnd.cinema_java_spring.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Setter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.djnd.cinema_java_spring.service.SessionManager;

/**
 * custom JWT Authentication Converter để validate session in process
 * authentication
 * validate sessionId in JWT token với database
 * reject authentication nếu session invalid
 * convert JWT claims to Spring Security authorities
 * jwt already decode from security config after to security authorities
 */
@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final SessionManager sessionManager;

    @Setter
    private String authorityPrefix = ""; // Prefix cho authorities (default empty)
    /**
     * -- SETTER --
     * set claim in JWT include permissions list
     */
    @Setter
    private String authoritiesClaimName = "permission"; // name claim include permissions

    public CustomJwtAuthenticationConverter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Convert JWT token to Spring Security authorities
     * - get email and sessionId từ JWT
     * - check session invalid with SessionManager
     * - if session invalid → throw exception → 401 response
     * - if session valid → convert authorities và next
     * 
     * @request "sub": "user@example.com",
     *          "sessionId": "abc123-def456",
     *          "permission": [
     *          "ROLE_USER_CREATE",
     *          "ROLE_BOOK_VIEW"
     *          ]
     * @return Collection<GrantedAuthority> authorities = [
     *         SimpleGrantedAuthority("ROLE_USER_CREATE"),
     *         SimpleGrantedAuthority("ROLE_BOOK_VIEW")]
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // get info JWT token
        String email = jwt.getSubject(); // email của user
        String sessionId = jwt.getClaimAsString("sessionId"); // session ID in token

        // validate session cho Single Session feature
        if (email != null && sessionId != null) {
            boolean isValidSession = sessionManager.isValidSession(email, sessionId);

            if (!isValidSession) {
                // session invalid → User đã login ở other
                // throw exception → Spring Security sẽ trả về 401
                // throw new RuntimeException("Session expired or invalid");
                throw new BadCredentialsException("Session expired or invalid. Please re-authenticate.");
            }
        }

        // Nếu session valid, convert JWT claims to authorities
        Collection<String> authorities = jwt.getClaimAsStringList(authoritiesClaimName);
        if (authorities == null || authorities.isEmpty()) {
            return List.of(); // no permissions
        }

        // Convert string permissions to Spring Security GrantedAuthority
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authorityPrefix + authority))
                .collect(Collectors.toList());
    }
}
