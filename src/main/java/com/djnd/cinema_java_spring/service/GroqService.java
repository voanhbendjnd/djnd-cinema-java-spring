package com.djnd.cinema_java_spring.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.djnd.cinema_java_spring.service.dto.ChatMessageDTO;
import com.djnd.cinema_java_spring.service.dto.GroqChatCompletionRequest;
import com.djnd.cinema_java_spring.service.dto.GroqChatCompletionResponse;

import lombok.RequiredArgsConstructor;

/**
 * Gọi Groq Cloud API (OpenAI-compatible) để lấy phản hồi chatbot.
 * Chạy bất đồng bộ trên thread pool riêng ("chatbotExecutor") để không block
 * luồng xử lý request chính của Spring MVC.
 */
@Service
@RequiredArgsConstructor
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý rạp phim thông minh của CineMate.
            Hãy trả lời câu hỏi của người dùng về phim ảnh một cách ngắn gọn, thân thiện, bằng tiếng Việt.
            Nếu người dùng hỏi gợi ý phim, hãy đề xuất 2-3 phim phù hợp và hỏi lại họ để hiểu rõ sở thích hơn.
            Không bịa thông tin về suất chiếu, giá vé, hoặc lịch chiếu cụ thể nếu không chắc chắn.
            """;

    private final RestTemplate chatbotRestTemplate;

    @Value("${djnd.groq.api-key}")
    private String apiKey;

    @Value("${djnd.groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Async("chatbotExecutor")
    public CompletableFuture<String> askAsync(String userMessage, List<ChatMessageDTO> history) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            GroqChatCompletionRequest requestBody = new GroqChatCompletionRequest(
                    model, SYSTEM_PROMPT, history, userMessage);
            HttpEntity<GroqChatCompletionRequest> entity = new HttpEntity<>(requestBody, headers);

            GroqChatCompletionResponse response = chatbotRestTemplate.postForObject(
                    GROQ_URL, entity, GroqChatCompletionResponse.class);

            if (response == null) {
                throw new IllegalStateException("Groq returned empty response body");
            }
            String reply = response.extractFirstMessageContent();
            return CompletableFuture.completedFuture(reply);
        } catch (Exception ex) {
            log.warn("[Chatbot] Groq call failed: {}", ex.getMessage());
            return CompletableFuture.failedFuture(ex);
        }
    }
}
