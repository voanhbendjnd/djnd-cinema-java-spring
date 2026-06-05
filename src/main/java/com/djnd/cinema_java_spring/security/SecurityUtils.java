package com.djnd.cinema_java_spring.security;

import com.djnd.cinema_java_spring.domain.entity.Permission;
import com.djnd.cinema_java_spring.domain.entity.Role;
import com.djnd.cinema_java_spring.service.dto.ResLoginDTO;
import com.nimbusds.jose.util.Base64;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtils {
    private final JwtEncoder jwtEncoder;
    public static final MacAlgorithm JWT_ALGORITHM;
    @Value("${djnd.jwt.base64-secret}")
    private String jwtKey;
    @Value("${djnd.jwt.access-token-validity-in-seconds}")
    private Long accessTokenExpiration;
    @Value("${djnd.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    static {
        JWT_ALGORITHM = MacAlgorithm.HS512;
    }

    public SecurityUtils(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(this.jwtKey).decode();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String login, ResLoginDTO dto, String sessionId, Role role) {
        ResLoginDTO.UserInsideToken userToken = new ResLoginDTO.UserInsideToken();
        userToken.setId(dto.getUser().getId());
        userToken.setLogin(login);
        userToken.setName(dto.getUser().getName());
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);
        List<String> listAuthority = new ArrayList<>();
        if (role != null) {
            listAuthority = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toList());
        }

        JwtClaimsSet claim = JwtClaimsSet.builder().issuedAt(now).expiresAt(validity).subject(login)
                .claim("user", userToken).claim("permission", listAuthority).claim("sessionId", sessionId).build();
        JwsHeader jwtHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwtHeader, claim)).getTokenValue();
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter((a) -> a.getCredentials() instanceof String).map((a) -> (String) a.getCredentials());
    }

    public static Optional<Long> getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal).filter((principal) -> principal instanceof Jwt)
                .map((principal) -> (Jwt) principal).map((jwt) -> jwt.getClaim("user"))
                .filter((userClaim) -> userClaim instanceof Map).map((userClaim) -> ((Map<?, ?>) userClaim).get("id"))
                .flatMap((id) -> {
                    if (id instanceof Number n) {
                        return Optional.of(n.longValue());
                    } else if (id instanceof String s) {
                        try {
                            return Optional.of(Long.parseLong(s));
                        } catch (NumberFormatException var4) {
                            return Optional.empty();
                        }
                    } else {
                        return Optional.empty();
                    }
                });
    }

    public static Long getCurrentUserIdOrNull() {
        Optional<Long> userId = getCurrentUserId();
        return userId.isPresent() ? (Long) userId.get() : null;
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else {
            Object var2;
            if ((var2 = authentication.getPrincipal()) instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) var2;
                return springSecurityUser.getUsername();
            } else {
                Object var4;
                if ((var4 = authentication.getPrincipal()) instanceof Jwt) {
                    Jwt jwt = (Jwt) var4;
                    return jwt.getSubject();
                } else {
                    Object var6;
                    if ((var6 = authentication.getPrincipal()) instanceof String) {
                        String s = (String) var6;
                        return s;
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    public Claims parseRefreshTokenIgnoreExpired(String token) {
        try {
            return (Claims) Jwts.parserBuilder().setSigningKey(this.getSecretKey()).build().parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        }
    }

    public Claims parseRefreshToken(String token) {
        return (Claims) Jwts.parserBuilder().setSigningKey(this.getSecretKey()).build().parseClaimsJws(token).getBody();
    }

    public String createRefreshToken(String login, ResLoginDTO dto) {
        ResLoginDTO.UserInsideToken userToken = new ResLoginDTO.UserInsideToken();
        userToken.setLogin(login);
        // userToken.setEmail(dto.getUser().getEmail());
        userToken.setId(dto.getUser().getId());
        userToken.setName(dto.getUser().getName());
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder().issuedAt(now).expiresAt(validity).subject(login)
                .claim("user", userToken).build();
        JwsHeader jwtHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwtHeader, claims)).getTokenValue();
    }

    public static Optional<String> getCurrentSessionId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Object var2;
        if (securityContext.getAuthentication() != null
                && (var2 = securityContext.getAuthentication().getPrincipal()) instanceof Jwt) {
            Jwt jwt = (Jwt) var2;
            return Optional.ofNullable(jwt.getClaimAsString("sessionId"));
        } else {
            return Optional.empty();
        }
    }
}
