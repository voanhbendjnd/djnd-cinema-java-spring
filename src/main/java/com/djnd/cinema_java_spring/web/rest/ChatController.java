package com.djnd.cinema_java_spring.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.service.dto.ChatRequestDTO;
import com.djnd.cinema_java_spring.service.dto.ChatResponseDTO;
import com.djnd.cinema_java_spring.service.ChatFacadeService;
import com.djnd.cinema_java_spring.config.Constants;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.VERSION_API + "/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatFacadeService chatFacadeService;

    @PostMapping("/message")
    @ApiMessage("Send chat message")
    public ResponseEntity<ChatResponseDTO> sendMessage(@Valid @RequestBody ChatRequestDTO request) {
        return ResponseEntity.ok(chatFacadeService.handleMessage(request));
    }
}
