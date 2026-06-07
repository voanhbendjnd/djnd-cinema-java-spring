package com.djnd.cinema_java_spring.config;

import java.util.Collections;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private static final String VERSION_API = "/api/v1";
    @Value("${djnd.jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            SmartAuthenticationEntryPoint sap,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfig
    // PublicEndpointFilter publicEndpointFilter
    ) throws Exception {
        String[] whiteList = {
                // "/**",
                "/error",
                "/storage/**",
                "/api/v1/search/**",
                "/ws/**",

        };
        http
                .cors(cors -> cors.configurationSource(corsConfig))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(HttpMethod.POST, VERSION_API + "/account/register").permitAll()
                                .requestMatchers(HttpMethod.POST, VERSION_API + "/auth/login").permitAll()
                                .requestMatchers(HttpMethod.GET, VERSION_API + "/account/activate/**").permitAll()
                                // .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                                // .requestMatchers(HttpMethod.GET, "/api/v1/comments/{id}").permitAll()
                                // .requestMatchers(HttpMethod.GET, "/api/v1/comments").permitAll()
                                .requestMatchers(whiteList).permitAll()
                                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(publicEndpointJwtAuthenticationTokenConverter()))
                        .authenticationEntryPoint(sap))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length,
                SecurityUtils.JWT_ALGORITHM.getName());
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtils.JWT_ALGORITHM).build();
        return token -> {
            try {
                // if token "undefined" -> throw
                return jwtDecoder.decode(token);
            } catch (Exception ex) {
                System.out.println(">>> JWT Error: " + ex.getMessage());
                throw ex;
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(CustomJwtAuthenticationConverter customConverter) {
        // config CustomJwtAuthenticationConverter for validate session
        customConverter.setAuthorityPrefix(""); // no prefix cho authorities
        customConverter.setAuthoritiesClaimName("permission"); // name claim include permissions in JWT

        // init JWT authentication converter with custom converter
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(customConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtAuthenticationConverter publicEndpointJwtAuthenticationTokenConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // for public endpoints, we can skip the JWT validation entirely
            // by returning empty authorities - the permitAll() will handle access
            return Collections.emptyList();
        });

        return converter;
    }

}