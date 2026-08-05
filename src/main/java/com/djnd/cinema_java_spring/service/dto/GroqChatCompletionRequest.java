package com.djnd.cinema_java_spring.service.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroqChatCompletionRequest {

    private String model;
    private List<Message> messages;
    private Double temperature = 0.7;
    private Integer max_tokens = 400;

    public GroqChatCompletionRequest() {
    }

    public GroqChatCompletionRequest(String model, String systemPrompt, List<ChatMessageDTO> history,
            String userMessage) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("system", systemPrompt));
        if (history != null) {
            for (ChatMessageDTO m : history) {
                this.messages.add(new Message(m.getRole(), m.getContent()));
            }
        }
        this.messages.add(new Message("user", userMessage));
    }

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
