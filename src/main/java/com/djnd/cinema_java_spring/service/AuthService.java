package com.djnd.cinema_java_spring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.ResLoginDTO;

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
    @Value("${djnd.jwt.access-token-validity-in-seconds}")
    private Long expiresIn;

    @Transactional
    public ResLoginDTO generateResLoginDTO(User user) {
        var res = new ResLoginDTO();
        var userLogin = new ResLoginDTO.UserLogin();
        userLogin.setLogin(user.getLogin());
        if (user.getEmail() != null) {
            userLogin.setEmail(user.getEmail());

        }
        userLogin.setName(user.getName());
        userLogin.setRole(user.getRole().getName());
        userLogin.setLoginWith(user.getLoginWith());
        res.setUser(userLogin);
        String sessionId = sessionManager.createNewSession(user);
        String accessToken = securityUtils.createAccessToken(user.getLogin(), res, sessionId, user.getRole());
        res.setAccessToken(accessToken);
        var newRefreshToken = this.securityUtils.createRefreshToken(user.getLogin(), res);
        user.setRefreshToken(newRefreshToken);
        res.setExpiresIn(expiresIn);
        return res;

    }

    public String getRefreshTokenByUserId(Long userId) {
        return userRepository.getRefreshTokenById(userId);
    }
}
