package com.djnd.cinema_java_spring.domain.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.djnd.cinema_java_spring.service.dto.ChatMessageDTO;

import lombok.Getter;

@Getter
public class ChatSession {

    private static final int MAX_HISTORY = 10;

    private final String sessionId;
    private final ConcurrentLinkedDeque<ChatMessageDTO> history = new ConcurrentLinkedDeque<>();
    private final Set<String> preferredGenres = new LinkedHashSet<>();
    private volatile Instant lastInteractionAt;

    public ChatSession(String sessionId) {
        this.sessionId = sessionId;
        this.lastInteractionAt = Instant.now();
    }

    public synchronized void appendMessage(ChatMessageDTO message) {
        history.addLast(message);
        while (history.size() > MAX_HISTORY) {
            history.pollFirst();
        }
        lastInteractionAt = Instant.now();
    }

    public void addPreferredGenre(String genre) {
        if (genre != null && !genre.isBlank()) {
            preferredGenres.add(genre);
        }
    }

    public List<ChatMessageDTO> getHistorySnapshot() {
        return new ArrayList<>(history);
    }

    public boolean isExpired(long ttlMinutes) {
        return Instant.now().isAfter(lastInteractionAt.plusSeconds(ttlMinutes * 60));
    }
}
