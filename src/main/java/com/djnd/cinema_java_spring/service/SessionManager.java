package com.djnd.cinema_java_spring.service;

import java.util.UUID;

import org.springframework.cache.CacheManager;
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
    final CacheManager cacheManager;

    public String createNewSession(Long userId) {
        String newSessionId = UUID.randomUUID().toString();
        int updated = userRepository.updateSessionById(userId, newSessionId);
        if (updated > 0) {
            return newSessionId;

        }
        throw new ResourceNotFoundException("User not found!");
    }

    public User getUserByLogin(String login) {
        return userRepository.findOneByLoginOrEmail(login.toLowerCase(), login.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    public boolean isValidSession(String login, String sessionId) {
        User user = this.getUserByLogin(login.toLowerCase());
        if (user.getSessionId() == null) {
            return false;
        }
        return user.getSessionId().equals(sessionId);
    }

    public void invalidateSession(String username) {
        final String finalUsername = username.toLowerCase();
        userRepository.findOneByLoginOrEmail(finalUsername, finalUsername).ifPresent(user -> {
            user.setSessionId(null);
            userRepository.save(user);

            // clearUserCache(finalUsername);
        });
    }

    // private void clearUserCache(String login) {
    // if (login != null) {

    // var cache = cacheManager.getCache(UserRepository.USERS_BY_LOGIN_EMAIL_CACHE);
    // if (cache != null) {
    // cache.evictIfPresent(login.toLowerCase());
    // }
    // }

    // }
}
