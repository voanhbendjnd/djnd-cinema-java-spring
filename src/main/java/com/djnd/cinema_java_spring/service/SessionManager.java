package com.djnd.cinema_java_spring.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SessionManager {

    final UserRepository userRepository;
    final UserService userService;

    public String createNewSession(Long userId) {
        String newSessionId = UUID.randomUUID().toString();
        int updated = userRepository.updateSessionById(userId, newSessionId);
        if (updated > 0) {
            return newSessionId;

        }
        throw new ResourceNotFoundException("User not found!");
    }

    public boolean isValidSession(String login, String sessionId) {
        return userService.getSecurityCacheByLogin(login.toLowerCase())
                .map(user -> sessionId.equals(user.getSessionId()))
                .orElse(false);
    }

    public void invalidateSession(String username) {
        final String finalUsername = username.toLowerCase();
        userRepository.findOneByLoginOrEmail(finalUsername, finalUsername).ifPresent(user -> {
            user.setSessionId(null);
            userRepository.save(user);
            userService.evictUserCache(user.getLogin(), user.getEmail());
        });
    }

}
