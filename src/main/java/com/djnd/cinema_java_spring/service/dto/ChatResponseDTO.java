package com.djnd.cinema_java_spring.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private String reply;
    private List<MovieSuggestionDTO> movies; // null nếu không có gợi ý phim kèm theo
    private boolean fallback; // true nếu Groq lỗi/timeout và đây là câu trả lời dự phòng
}
