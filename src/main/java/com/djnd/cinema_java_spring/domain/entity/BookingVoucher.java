package com.djnd.cinema_java_spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_voucher")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingVoucher extends AbstractAuditingEntity<Long> implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "booking_id")
        Booking booking;
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "voucher_id")
        Promotion voucher;
        Double discountPercentage;
        BigDecimal finalAmount;
        BigDecimal originalAmount;
        BigDecimal discountAmount;

}
