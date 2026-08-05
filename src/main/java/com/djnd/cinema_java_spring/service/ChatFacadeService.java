package com.djnd.cinema_java_spring.service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.domain.entity.ChatSession;
import com.djnd.cinema_java_spring.service.dto.ChatMessageDTO;
import com.djnd.cinema_java_spring.service.dto.ChatRequestDTO;
import com.djnd.cinema_java_spring.service.dto.ChatResponseDTO;
import com.djnd.cinema_java_spring.service.dto.MovieSuggestionDTO;

import lombok.RequiredArgsConstructor;

/**
 * Điều phối toàn bộ luồng xử lý 1 tin nhắn chat:
 * 1. Lấy/tạo session, append tin nhắn user vào history
 * 2. Gọi Groq (bất đồng bộ) với timeout giới hạn
 * 3. Nếu thành công -> lưu history + trả reply
 * 4. Nếu lỗi/timeout -> fallback sang top phim bán chạy + tin nhắn mặc định
 */
@Service
@RequiredArgsConstructor
public class ChatFacadeService {

    private static final Logger log = LoggerFactory.getLogger(ChatFacadeService.class);
    private static final long GROQ_TIMEOUT_SECONDS = 6; // > read timeout của RestTemplate (5s) để chừa buffer
    private static final String FALLBACK_MESSAGE = "Hiện tại mình hơi bận, nhưng bạn có thể tham khảo những phim hot này nhé!";

    private final SessionManager sessionManager;
    private final GroqService groqService;
    private final MovieFallbackService fallbackService;

    public ChatResponseDTO handleMessage(ChatRequestDTO request) {
        ChatSession session = sessionManager.getOrCreate(request.getSessionId());
        session.appendMessage(new ChatMessageDTO("user", request.getMessage()));

        try {
            String reply = groqService
                    .askAsync(request.getMessage(), session.getHistorySnapshot())
                    .get(GROQ_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            session.appendMessage(new ChatMessageDTO("assistant", reply));

            return ChatResponseDTO.builder()
                    .reply(reply)
                    .fallback(false)
                    .build();

        } catch (TimeoutException | ExecutionException | InterruptedException ex) {
            log.warn("[Chatbot] Falling back to top movies for session {}: {}",
                    request.getSessionId(), ex.getMessage());

            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            List<MovieSuggestionDTO> movies = fallbackService.getTopMovies(3);
            session.appendMessage(new ChatMessageDTO("assistant", FALLBACK_MESSAGE));

            return ChatResponseDTO.builder()
                    .reply(FALLBACK_MESSAGE)
                    .movies(movies)
                    .fallback(true)
                    .build();
        }
    }
}
