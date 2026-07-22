package com.djnd.cinema_java_spring.repository;

import com.djnd.cinema_java_spring.domain.entity.BookingVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingVoucherRepository extends JpaRepository<BookingVoucher, Long> {
    @Query(value = "select bv from BookingVoucher bv where bv.booking.id = :bookingId")
    Optional<BookingVoucher> findByBookingId(@Param("bookingId") Long bookingId);
}
