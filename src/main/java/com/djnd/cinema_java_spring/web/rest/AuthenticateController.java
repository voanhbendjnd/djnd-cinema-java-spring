package com.djnd.cinema_java_spring.web.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.config.Constants;
import com.djnd.cinema_java_spring.security.CustomUserDetails;
import com.djnd.cinema_java_spring.service.AuthService;
import com.djnd.cinema_java_spring.service.dto.ResLoginDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.vm.LoginVM;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/auth")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthenticateController {
    final AuthenticationManagerBuilder authenticationManagerBuilder;
    final AuthService authService;
    @Value("${djnd.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    @PostMapping("/login")
    @ApiMessage("Login with credentials")
    public ResponseEntity<ResLoginDTO> loginCredentials(@RequestBody LoginVM dto) {
        var usernamePassowrdAuthenticationToken = new UsernamePasswordAuthenticationToken(dto.getLogin().toLowerCase(),
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

    @PostMapping("/logout")
    @ApiMessage("Logout")
    public ResponseEntity<String> logout(
            @CookieValue(name = "refresh_token", defaultValue = Constants.REFRESH_TOKEN_EXPIRED) String refreshToken) {
        if (refreshToken.equals(Constants.REFRESH_TOKEN_EXPIRED)) {
            throw new BadCredentialsException("Refresh token invalid!");
        }
        authService.logout(refreshToken);
        return ResponseEntity.ok("Logout success");
    }

    @PostMapping("/refresh")
    @ApiMessage("Refresh token")
    public ResponseEntity<ResLoginDTO> refreshToken(
            @CookieValue(name = "refresh_token", required = true) String refreshToken) {
        if (refreshToken == null) {
            throw new RequestInvalidException("Not found refresh token trong cookie!");
        }
        var resLoginDTO = authService.processRefreshToken(refreshToken);
        var cookie = ResponseCookie
                .from("refresh_token", authService.getRefreshTokenByUserId(resLoginDTO.getUser().getId()))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(resLoginDTO);

    }
}
