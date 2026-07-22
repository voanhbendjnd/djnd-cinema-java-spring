package com.djnd.cinema_java_spring.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Auditable;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "transaction_histories", uniqueConstraints =  @UniqueConstraint(columnNames = {"action", "ticket_id"}))
public class TransactionHistory extends AbstractAuditingEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @NotNull
    @Column(nullable = false, name = "amount")
    BigDecimal amount;
    @Column(name = "action", nullable = false)
    String action;
    String reason;
    @NotNull
    @Column(name = "ticket_id", nullable = false)
    Long ticketId;
}
