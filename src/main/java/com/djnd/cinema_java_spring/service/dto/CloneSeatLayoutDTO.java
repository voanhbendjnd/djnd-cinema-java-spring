package com.djnd.cinema_java_spring.service.dto;

import java.math.BigDecimal;

import com.djnd.cinema_java_spring.domain.enumeration.SeatStatus;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloneSeatLayoutDTO {
    Integer id;
    String seatRow;
    Integer seatNo;
    SeatType type;
    SeatStatus status;
    String bookingStatus;
    BigDecimal price;
}
