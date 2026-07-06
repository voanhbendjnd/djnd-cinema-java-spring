package com.djnd.cinema_java_spring.service.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;

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
public class PublishBookingDTO {
    Long id;
    String createdBy;

    Instant createdDate;

    String lastModifiedBy;

    Instant lastModifiedDate;

    String bookingCode;

    String paymentMethod;

    BookingStatus status;

    BigDecimal totalAmount;
}
