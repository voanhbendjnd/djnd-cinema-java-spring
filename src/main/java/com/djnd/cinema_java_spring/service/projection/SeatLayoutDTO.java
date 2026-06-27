package com.djnd.cinema_java_spring.service.projection;

import java.io.Serial;
import java.io.Serializable;

import com.djnd.cinema_java_spring.domain.enumeration.SeatStatus;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;

public record SeatLayoutDTO(Integer id, String seatRow, Integer seatNo,
        SeatType type, SeatStatus status, String bookingStatus) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
