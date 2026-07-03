package com.djnd.cinema_java_spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.BookingDetail;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    @Modifying
    @Query(value = "delete from BookingDetail b where b.booking.id in :bookingIds")
    int deleteByBookingIdIn(@Param("bookingIds") List<Long> ids);
}
