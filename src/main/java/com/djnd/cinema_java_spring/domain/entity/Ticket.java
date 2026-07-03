package com.djnd.cinema_java_spring.domain.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tickets", uniqueConstraints = @UniqueConstraint(columnNames = { "showtime_id", "seat_id" }))
public class Ticket extends AbstractAuditingEntity<Long> implements Serializable {
    @Serial
    private final static long SerialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id")
    Showtime showtime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    Seat seat;
    @NotNull
    BigDecimal price;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    Booking booking;
    @Column(name = "code", unique = true)
    String code;
}
