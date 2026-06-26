package com.djnd.cinema_java_spring.config;

import java.util.List;

import javax.crypto.SecretKey;
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

import com.djnd.cinema_java_spring.security.GuestAwareJwtDecoder;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

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
            JwtAuthenticationConverter jwtConverter,
            JwtDecoder baseJwtDecoder,
            HttpServletRequest request
    // PublicEndpointFilter publicEndpointFilter
    ) throws Exception {
        String[] whiteList = {
                "/error",
                "/storage/**",
                "/api/v1/search/**",
                "/ws/**",
                "/api/v1/files/**"
        };
        List<String> publishEndpoints = List.of(Constants.VERSION_API + "/home/movies",
                Constants.VERSION_API + "/account/register",
                Constants.VERSION_API + "/auth/login",
                Constants.VERSION_API + "/auth/refresh",
                Constants.VERSION_API + "/account/activate/**",
                Constants.VERSION_API + "/account/reset-password/init",
                Constants.VERSION_API + "/account/reset-password/finish",
                Constants.VERSION_API + "/files/**");
        http
                .cors(cors -> cors.configurationSource(corsConfig))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                // guest
                                .requestMatchers(HttpMethod.GET, Constants.VERSION_API + "/home/movies").permitAll()
                                .requestMatchers(HttpMethod.POST, Constants.VERSION_API + "/account/register")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, Constants.VERSION_API + "/auth/login").permitAll()
                                .requestMatchers(HttpMethod.GET, Constants.VERSION_API + "/movies/{id}/showtimes")
                                .permitAll()
                                // user
                                .requestMatchers(HttpMethod.POST, Constants.VERSION_API + "/auth/refresh").permitAll()
                                .requestMatchers(HttpMethod.GET, Constants.VERSION_API + "/account/activate/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST,
                                        Constants.VERSION_API + "/account/reset-password/init")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST,
                                        Constants.VERSION_API + "/account/reset-password/finish")
                                .permitAll()

                                .requestMatchers(whiteList).permitAll()
                                .anyRequest().authenticated())
                // check token when fe send request
                .oauth2ResourceServer(oauth2 -> oauth2
                        // decode and check valid expire token -> jwt auth converter get permission ->
                        // security context
                        // if token invalid -> sap
                        // encode bearer token
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtConverter)
                                .decoder(new GuestAwareJwtDecoder(baseJwtDecoder, request, publishEndpoints)))
                        .authenticationEntryPoint(sap))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey()));
    }

    /**
     * create secret key and check follow hs256, hs384, hs512
     * 
     * @return
     */
    @Bean
    public SecretKey jwtSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey())
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