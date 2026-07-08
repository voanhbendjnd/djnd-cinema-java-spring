package com.djnd.cinema_java_spring.repository;

import java.util.List;

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
}
