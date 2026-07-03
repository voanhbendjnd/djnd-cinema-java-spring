package com.djnd.cinema_java_spring.service.dto;

import java.time.LocalDateTime;

import com.djnd.cinema_java_spring.domain.enumeration.RoomType;

public record ShowTimeResponse(
                Long id,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime,
                Integer roomId,
                String roomName,
                RoomType roomType) {

}
