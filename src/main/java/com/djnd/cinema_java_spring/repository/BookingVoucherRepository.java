package com.djnd.cinema_java_spring.repository;

import com.djnd.cinema_java_spring.domain.entity.BookingVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingVoucherRepository extends JpaRepository<BookingVoucher, Long> {
}
