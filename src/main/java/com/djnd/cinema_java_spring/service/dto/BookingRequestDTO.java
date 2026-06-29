package com.djnd.cinema_java_spring.service.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestDTO {
    Long showtimeId;
    List<Integer> seatIds;
    String paymentMethod;
    String dayType;

}
