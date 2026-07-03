package com.djnd.cinema_java_spring.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    /**
     * mix op and pess lock with version
     * FORCE_INCREMENT before end transaction it update version + 1 unit
     * 
     * @param bookingId
     * @return
     */
    // @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = { "bookingDetails", "bookingDetails.seat", "bookingDetails.showtime" })
    @Query(value = "select b from Booking b where b.id = :bookingId")
    Optional<Booking> findForUpdateDetailByIdWithVersion(@Param("bookingId") Long bookingId);

    @Query(value = "select b from Booking b where b.status = :status and b.createdDate >= :threshold")
    List<Booking> findAllByStatusAndCreatedAtBefore(@Param("status") BookingStatus status,
            @Param("threshold") Instant threhold);

}
