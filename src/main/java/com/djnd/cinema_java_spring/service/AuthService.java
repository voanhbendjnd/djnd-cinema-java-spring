package com.djnd.cinema_java_spring.service;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.ResLoginDTO;
import com.djnd.cinema_java_spring.service.dto.UserSecurityCacheDTO;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import io.jsonwebtoken.Claims;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthService {
    final SessionManager sessionManager;
    final SecurityUtils securityUtils;
    final UserRepository userRepository;
    final UserService userService;
    @Value("${djnd.jwt.access-token-validity-in-seconds}")
    private Long expiresIn;

    @Transactional
    public ResLoginDTO generateResLoginDTO(UserSecurityCacheDTO user) {
        var res = new ResLoginDTO();
        var userLogin = new ResLoginDTO.UserLogin();
        userLogin.setLogin(user.getLogin());
        if (user.getEmail() != null) {
            userLogin.setEmail(user.getEmail());

        }
        userLogin.setId(user.getId());
        userLogin.setGender(user.getGender());
        userLogin.setName(user.getName());
        userLogin.setRole(user.getRole());
        userLogin.setLoginWith(user.getLoginWith());
        res.setUser(userLogin);
        String sessionId = sessionManager.createNewSession(user.getId());
        userService.evictUserCache(user.getLogin(), user.getEmail());
        var permissions = user.getPermissions();
        permissions.add(user.getRole());
        String accessToken = securityUtils.createAccessToken(user.getLogin(), res, sessionId,
                permissions);
        res.setAccessToken(accessToken);
        var newRefreshToken = this.securityUtils.createRefreshToken(user.getLogin(), res);
        int updated = userRepository.updateRefreshTokenById(user.getId(), newRefreshToken);
        if (updated <= 0) {
            throw new ResourceNotFoundException("User not found!");
        }
        res.setExpiresIn(expiresIn);
        return res;

    }

    public String getRefreshTokenByUserId(Long userId) {
        return userRepository.getRefreshTokenById(userId);
    }

    @Transactional
    public void logout(String refreshToken) {
        Claims claims = this.securityUtils.parseRefreshTokenIgnoreExpired(refreshToken);
        var username = claims.getSubject();
        if (username == null) {
            throw new BadCredentialsException("Login not found!");
        }
        if (new EmailValidator().isValid(username, null)) {
            var updated = userRepository.resetRefreshTokenByEmail(username);
            if (updated <= 0) {
                throw new ResourceNotFoundException("User not found!");
            }
        } else {
            var updated = userRepository.resetRefreshTokenByLogin(username);
            if (updated <= 0) {
                throw new ResourceNotFoundException("User not found!");
            }

        }
        sessionManager.invalidateSession(username);

    }
}
