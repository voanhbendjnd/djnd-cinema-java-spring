package com.djnd.cinema_java_spring.domain.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "bookings")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking extends AbstractAuditingEntity<Long> implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "booking_code", unique = true, nullable = false)
    String bookingCode;
    @NotNull
    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status", nullable = false)
    BookingStatus status;
    @NotNull
    @Column(name = "payment_method", nullable = false)
    String paymentMethod;
    @Version
    Long version = 0L;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    Customer customer;
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BookingDetail> bookingDetails;
    @OneToMany(mappedBy = "booking", cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    List<PaymentHistory> paymentHistories;

    @OneToOne(mappedBy = "booking", cascade = {CascadeType.MERGE, CascadeType.PERSIST })
    BookingVoucher bookingVoucher;

}
