package com.djnd.cinema_java_spring.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "seat_maintenances")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatMaintenance extends AbstractAuditingEntity<Integer> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Integer id;
    @NotNull
    @Column(name = "seat_id", nullable = false)
    Integer seatId;
    @NotNull
    @Column(name = "start_time", nullable = false)
    LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    LocalDateTime endTime;
    String reason;
}
