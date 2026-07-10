package com.djnd.cinema_java_spring.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.djnd.cinema_java_spring.domain.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.CustomerVoucher;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, Long> {

    @Query(value = "select v.title from CustomerVoucher cv join cv.voucher v where cv.customer.userId = :customerId and v.id in :voucherIds")
    List<String> getTitleVoucherByCustomerClaim(@Param("customer_id") Long customerId,
            @Param("voucherIds") List<Long> voucherIds);

    @Query(value = """
    select v from CustomerVoucher cv join cv.voucher v where cv.customer.userId = :customerId and v.isActive = true
    and (:cursor is null or v.startTime < :cursor or (v.starttime = :cursor and v.id < :voucherId))
    order by v.startTime desc, v.id desc
""")
    List<Promotion> getVoucherCustomerWithCursor(@Param("customerId") Long customerId,@Param("voucherId") Long voucherId, @Param("cursor") LocalDateTime startTime, Pageable pageable);

}
