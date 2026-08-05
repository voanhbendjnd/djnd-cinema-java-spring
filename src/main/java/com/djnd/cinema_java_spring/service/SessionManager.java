package com.djnd.cinema_java_spring.service;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.domain.entity.ChatSession;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SessionManager {

    final UserRepository userRepository;
    final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    private static final long TTL_MINUTES = 30;
    private static final long CLEANUP_INTERVAL_MINUTES = 5;
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "chatbot-session-cleaner");
        t.setDaemon(true);
        return t;
    });

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

    @PostConstruct
    public void startCleanupScheduler() {
        cleaner.scheduleAtFixedRate(this::evictExpiredSessions,
                CLEANUP_INTERVAL_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    public ChatSession getOrCreate(String sessionId) {
        return sessions.computeIfAbsent(sessionId, ChatSession::new);
    }

    public int activeSessionCount() {
        return sessions.size();
    }

    private void evictExpiredSessions() {
        int before = sessions.size();
        sessions.entrySet().removeIf(e -> e.getValue().isExpired(TTL_MINUTES));
        int removed = before - sessions.size();
        if (removed > 0) {
            log.debug("[Chatbot] Evicted {} expired chat sessions ({} remaining)", removed, sessions.size());
        }
    }

    @PreDestroy
    public void shutdown() {
        cleaner.shutdownNow();
    }
}
