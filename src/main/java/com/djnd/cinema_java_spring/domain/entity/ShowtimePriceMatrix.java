package com.djnd.cinema_java_spring.domain.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

import com.djnd.cinema_java_spring.domain.enumeration.SeatType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "showtime_price_matrix")
public class ShowtimePriceMatrix extends AbstractAuditingEntity<Integer> implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(name = "day_type", length = 20)
    String dayType;
    @Column(name = "seat_type")
    @Enumerated(EnumType.STRING)
    SeatType seatType;
    LocalTime startTimeFrom;
    LocalTime startTimeTo;
    @NotNull
    @Column(name = "final_price", nullable = false)
    BigDecimal finalPrice;
}
