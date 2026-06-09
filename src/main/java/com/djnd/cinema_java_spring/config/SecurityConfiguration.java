package com.djnd.cinema_java_spring.config;

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
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfig,
            JwtAuthenticationConverter jwtConverter
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
                                .requestMatchers(HttpMethod.POST, Constants.VERSION_API + "/account/register")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, Constants.VERSION_API + "/auth/login").permitAll()
                                .requestMatchers(HttpMethod.GET, Constants.VERSION_API + "/account/activate/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST,
                                        Constants.VERSION_API + "/account/reset-password/init")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST,
                                        Constants.VERSION_API + "/account/reset-password/finish")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/account/change-password").permitAll()
                                .requestMatchers(whiteList).permitAll()
                                .anyRequest().authenticated())
                // check token when fe send request
                .oauth2ResourceServer(oauth2 -> oauth2
                        // decode and check valid expire token -> jwt auth converter get permission ->
                        // security context
                        // if token invalid -> sap
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
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

}