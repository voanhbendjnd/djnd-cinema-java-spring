package com.djnd.cinema_java_spring.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.entity.BookingDetail;
import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.entity.Showtime;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BookingDetailService {

    public BookingDetail generateBookingDetail(Booking booking, Showtime showtime, Seat seat, BigDecimal costSeat) {
        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);
        detail.setPrice(costSeat);
        detail.setShowtime(showtime);
        detail.setSeat(seat);
        return detail;
    }
}
