package com.djnd.cinema_java_spring.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.djnd.cinema_java_spring.domain.enumeration.BookingDetailStatus;
import com.djnd.cinema_java_spring.repository.BookingDetailRepository;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.UserAccessDeniedException;
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
    final BookingDetailRepository bookingDetailRepository;
    public BookingDetail generateBookingDetail(Booking booking, Showtime showtime, Seat seat, BigDecimal costSeat) {
        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);
        detail.setPrice(costSeat);
        detail.setShowtime(showtime);
        detail.setSeat(seat);
        detail.setStatus(BookingDetailStatus.LOCK.toString());
        return detail;
    }
    /*
    * Check booking detail for customer booking use ticket change to point
    * ticket booking detail will unlock if ticket change to point success
    * and other people can create booking again with seat and showtime with it
    * target method change detail from lock to unlock
    * */
    public void changeDetailFromLockToUnlock(List<BookingDetail> bookingDetails){

        bookingDetails.forEach(bookingDetail -> {
            bookingDetail.setStatus(BookingDetailStatus.UNLOCK.toString());
        });
        bookingDetailRepository.saveAll(bookingDetails);

    }

    public void checkAlreadyShowtimeSeatWithStatusLock(List<BookingDetail> bookingDetails){
        Long showtimeId = bookingDetails.getFirst().getShowtime().getId();
        Integer seatId = bookingDetails.getFirst().getSeat().getId();
        var bookingDetailsWithStatusLockExisting = bookingDetailRepository.getBookingDetailWithShowtimeIdAndSeatIdAndStatus(showtimeId, seatId, BookingDetailStatus.LOCK.toString());
        if(bookingDetailsWithStatusLockExisting != null && !bookingDetailsWithStatusLockExisting.isEmpty()){
            List<String> errorMessages = new ArrayList<>();
            for(BookingDetail bookingDetail : bookingDetailsWithStatusLockExisting){
                errorMessages.add(String.format("Has error at booking detail relation showtime id, seat id and status lock already exist at booking detail id [%s]", bookingDetail.getId()));
            }
            throw new RequestInvalidException(String.join("\n", errorMessages));

        }
    }
}
