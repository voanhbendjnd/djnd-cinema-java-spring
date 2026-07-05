package com.djnd.cinema_java_spring.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.djnd.cinema_java_spring.domain.enumeration.SeatType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    Long id;
    String movieTitle;
    LocalDateTime bookingAt;
    LocalDate releaseDate;
    String seatPosition;
    SeatType seatType;
    LocalTime startDateTime;
    LocalTime endDateTime;
    String paymentMethod;
    String createdBy;
    String cashBy;
    String ticketCode;
    BigDecimal price;
}
