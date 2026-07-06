package com.djnd.cinema_java_spring.service.realtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatRealtime {
    final SimpMessagingTemplate messagingTemplate;

    public void sendSeatSold(Long showtimeId, List<Integer> seatIds) {
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("bookingStatus", "SOLD");
        wsPayload.put("seatIds", seatIds);
        messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId + "/seats", wsPayload);
    }
}
