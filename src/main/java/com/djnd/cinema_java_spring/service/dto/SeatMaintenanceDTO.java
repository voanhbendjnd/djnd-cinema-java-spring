package com.djnd.cinema_java_spring.service.dto;

import jakarta.validation.constraints.NotNull;
import jdk.jfr.Registered;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatMaintenanceDTO {
    Integer id;
    @NotNull(message = "Seat ID not found!")
    Integer seatId;
    @NotNull(message = "Start time not found!")
    LocalDateTime startTime;
    @NotNull(message = "End time not found!")
    LocalDateTime endTime;
    String reason;
}
