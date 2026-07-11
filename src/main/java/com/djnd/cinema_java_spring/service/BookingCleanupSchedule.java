package com.djnd.cinema_java_spring.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.repository.BookingDetailRepository;
import com.djnd.cinema_java_spring.repository.BookingRepository;
import com.djnd.cinema_java_spring.service.projection.BookingSeatProjection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BookingCleanupSchedule {
    final BookingRepository bookingRepository;
    final BookingDetailRepository bookingDetailRepository;
    final BookingService bookingService;

    @Scheduled(fixedRate = 60000) // 60s
    @Transactional
    public void cleanupExpiredBookings() {
        Instant threshold = Instant.now().minus(10, ChronoUnit.MINUTES);
        List<BookingSeatProjection> expiredItems = bookingDetailRepository
                .findExpiredBookingSeats(BookingStatus.PENDING, threshold);
        if (expiredItems != null && !expiredItems.isEmpty()) {
            List<Long> bookingIds = expiredItems.stream().map(BookingSeatProjection::bookingId).toList();
            bookingRepository.updateStatusByIdIn(BookingStatus.CANCELLED, bookingIds);
            bookingDetailRepository.deleteByBookingIdIn(bookingIds);

            Map<Long, List<Integer>> seatsByShowtime = expiredItems.stream()
                    .collect(Collectors.groupingBy(BookingSeatProjection::showtimeId,
                            Collectors.mapping(BookingSeatProjection::seatId, Collectors.toList())));
            seatsByShowtime.forEach((showtimeId, seatIds) -> {
                String showtimeRedisKey = "showtime:" + showtimeId + ":seats";
                bookingService.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, seatIds);
            });
        }
    }
}