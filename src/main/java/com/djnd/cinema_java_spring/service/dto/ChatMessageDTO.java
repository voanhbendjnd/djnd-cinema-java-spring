package com.djnd.cinema_java_spring.service.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String role; // "user" | "assistant"
    private String content;
    private Instant timestamp;

    public ChatMessageDTO(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = Instant.now();
    }
}
