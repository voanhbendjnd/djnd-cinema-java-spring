package com.djnd.cinema_java_spring.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.BookingDetail;
import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.service.projection.BookingSeatProjection;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
        @Modifying
        @Query(value = "delete from BookingDetail b where b.booking.id in :bookingIds")
        int deleteByBookingIdIn(@Param("bookingIds") List<Long> ids);

        @Query("SELECT bd.booking.id AS bookingId, " +
                        "bd.showtime.id AS showtimeId, " +
                        "bd.seat.id AS seatId " +
                        "FROM BookingDetail bd " +
                        "WHERE bd.booking.status = :status " +
                        "AND bd.booking.createdDate <= :threshold")
        List<BookingSeatProjection> findExpiredBookingSeats(
                        @Param("status") BookingStatus status,
                        @Param("threshold") Instant threshold);
}
