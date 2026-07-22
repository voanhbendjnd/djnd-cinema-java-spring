package com.djnd.cinema_java_spring.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRefundInfoDTO {
    Long ticketId;
    String ticketCode;
    String movieTitle;
    String seatPosition;
    LocalDateTime showtime;
    BigDecimal originalAmount;
    BigDecimal refundAmount;
    String bookingCode;
    String customerEmail;
}
