package com.djnd.cinema_java_spring.service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroqChatCompletionResponse {

    private List<Choice> choices;
    
    public String extractFirstMessageContent() {
        if (choices == null || choices.isEmpty() || choices.get(0).getMessage() == null) {
            throw new IllegalStateException("Groq response has no choices");
        }
        String content = choices.get(0).getMessage().getContent();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Groq response content is blank");
        }
        return content;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
    }
}
