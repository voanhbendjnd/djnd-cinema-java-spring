package com.djnd.cinema_java_spring.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.config.CustomUserDetails;
import com.djnd.cinema_java_spring.service.AuthService;
import com.djnd.cinema_java_spring.service.dto.ReqLoginDTO;
import com.djnd.cinema_java_spring.service.dto.ResLoginDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.util.exception.RequestInvalidException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/auth")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthControllerV1 {
    final AuthenticationManagerBuilder authenticationManagerBuilder;
    final AuthService authService;
    @Value("${djnd.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    @PostMapping("/login")
    @ApiMessage("Login with credentials")
    public ResponseEntity<ResLoginDTO> loginCredentials(@RequestBody ReqLoginDTO dto) {
        var usernamePassowrdAuthenticationToken = new UsernamePasswordAuthenticationToken(dto.getLogin(),
                dto.getPassword());
        try {
            Authentication auth = authenticationManagerBuilder.getObject()
                    .authenticate(usernamePassowrdAuthenticationToken);
            SecurityContextHolder.getContext().setAuthentication(auth);
            var userDetails = (CustomUserDetails) auth.getPrincipal();
            var user = userDetails.user();
            var res = authService.generateResLoginDTO(user);
            ResponseCookie cookie = ResponseCookie
                    .from("refresh_token", authService.getRefreshTokenByUserId(user.getId()))
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(res);
        } catch (BadCredentialsException ex) {
            throw new RequestInvalidException("Login or password incorrect!");
        }

    }
}
