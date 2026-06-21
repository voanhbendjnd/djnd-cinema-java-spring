package com.djnd.cinema_java_spring.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieRoomTimeDTORequest {
    @NotNull(message = "Duration not found!")
    Integer duration;
    @NotNull(message = "Date not found!")
    LocalDate date;
    @NotNull(message = "Time not found!")
    LocalTime time;
    @NotBlank(message = "Room name not found!")
    String roomName;
    @NotNull(message = "Room ID not found!")
    Integer roomId;
}
