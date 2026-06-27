package com.djnd.cinema_java_spring.service.dto;

import java.util.List;

import com.djnd.cinema_java_spring.service.projection.SeatLayoutDTO;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResSeatAtRoomBookingDTO {
    Integer totalSeats;
    Integer totalSoldSeats;
    List<SeatLayoutDTO> seats;
}
