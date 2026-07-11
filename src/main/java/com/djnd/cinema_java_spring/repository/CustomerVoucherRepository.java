package com.djnd.cinema_java_spring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.djnd.cinema_java_spring.domain.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.CustomerVoucher;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, Long> {

        @Query(value = "select v.title from CustomerVoucher cv join cv.voucher v where cv.customer.userId = :customerId and v.id in :voucherIds")
        List<String> getTitleVoucherByCustomerClaim(@Param("customerId") Long customerId,
                        @Param("voucherIds") List<Long> voucherIds);

        @Query(value = """
                            select v from CustomerVoucher cv join cv.voucher v where cv.customer.userId = :customerId and v.isActive = true
                                       and v.startTime >= :current and cv.isUsed = false
                            and (:cursor is null or v.startTime < :cursor or (v.startTime = :cursor and v.id < :voucherId))
                            order by v.startTime desc, v.id desc
                        """)
        List<Promotion> getVoucherCustomerWithCursor(@Param("customerId") Long customerId,
                        @Param("voucherId") Long voucherId, @Param("cursor") LocalDateTime startTime,
                        @Param("current") LocalDateTime current, Pageable pageable);

        @Query(value = "select exists(select 1 from CustomerVoucher cv where cv.customer.userId = :customerId and cv.voucher.id = :voucherId)")
        boolean customerVoucherExist(@Param("customerId") Long customerId, @Param("voucherId") Long voucherId);

        @Query(value = "select cv from CustomerVoucher cv where cv.customer.userId = :customerId and cv.voucher.id = :voucherId")
        Optional<CustomerVoucher> findByCustomerIdAndVoucherId(@Param("customerId") Long customerId,
                        @Param("voucherId") Long voucherId);

        @Modifying
        @Query(value = "update CustomerVoucher cv set cv.isUsed = true where cv.customer.userId = :customerId and cv.voucher.id = :voucherid")
        void markVoucherAlreadyUsed(@Param("customerId") Long customerId, @Param("voucherId") Long voucherId);
}
