package com.djnd.cinema_java_spring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = { "bookingDetails", "bookingDetails.seat", "bookingDetails.showtime" })
    @Query(value = "select b from Booking b where b.id = :bookingId")
    Optional<Booking> findWithDetailById(@Param("bookingId") Long bookingId);
}
