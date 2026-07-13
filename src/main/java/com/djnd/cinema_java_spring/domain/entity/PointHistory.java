package com.djnd.cinema_java_spring.domain.entity;

import com.djnd.cinema_java_spring.domain.enumeration.PointTransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "point_histories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistory extends AbstractAuditingEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @NotNull
    @Column(name = "customer_id",  nullable = false)
    Long customerId;
    @NotNull
    @Column(name = "amount_points", nullable = false)
    Integer amountPoints;
    @NotNull
    @Column(name = "type", nullable = false)
    PointTransactionType type;
    String description;
}
