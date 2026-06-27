package com.djnd.cinema_java_spring.service;

import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.repository.SeatRepository;
import com.djnd.cinema_java_spring.service.dto.ResSeatAtRoomBookingDTO;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
@RequiredArgsConstructor
public class SeatService {
    final SeatRepository seatRepository;

    public ResSeatAtRoomBookingDTO getSeatAtRoomLayoutByShowtime(Long showtimeId) {
        var res = new ResSeatAtRoomBookingDTO();
        var seats = seatRepository.getSeatLayoutByShowtime(showtimeId);
        long totalSoldSeats = seats.stream().filter(seat -> seat.bookingStatus().equalsIgnoreCase("SOLD"))
                .mapToInt(s -> s.id()).count();
        res.setSeats(seats);
        res.setTotalSeats(seats.size());
        res.setTotalSoldSeats(Math.toIntExact(totalSoldSeats));
        return res;
    }
}
