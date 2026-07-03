package com.djnd.cinema_java_spring.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.repository.BookingDetailRepository;
import com.djnd.cinema_java_spring.repository.BookingRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BookingCleanupSchedule {
    final BookingRepository bookingRepository;
    final BookingDetailRepository bookingDetailRepository;

    @Scheduled(fixedRate = 60000) // 60s
    @Transactional
    public void cleanupExpiredBookings() {
        Instant threshold = Instant.now().plus(10, ChronoUnit.MINUTES);
        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndCreatedAtBefore(BookingStatus.PENDING,
                threshold);
        expiredBookings.forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
        });
        List<Long> bookingIds = expiredBookings.stream().map(Booking::getId).toList();
        if (bookingIds != null && !bookingIds.isEmpty()) {
            bookingDetailRepository.deleteByBookingIdIn(bookingIds);
            // list showtime key
            bookingRepository.saveAll(expiredBookings);
        }
    }
}