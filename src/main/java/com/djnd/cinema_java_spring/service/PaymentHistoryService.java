package com.djnd.cinema_java_spring.service;

import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.entity.PaymentHistory;
import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.repository.PaymentHistoryRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PaymentHistoryService {
    final PaymentHistoryRepository paymentHistoryRepository;

    public void createHistoryWithStatus(Booking booking, BookingStatus status) {
        PaymentHistory history = new PaymentHistory();
        history.setBooking(booking);
        history.setStatus(status);
        paymentHistoryRepository.save(history);
    }
}
