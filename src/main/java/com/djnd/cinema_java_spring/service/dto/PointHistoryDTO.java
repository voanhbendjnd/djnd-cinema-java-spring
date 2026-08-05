package com.djnd.cinema_java_spring.service.dto;

import com.djnd.cinema_java_spring.domain.enumeration.PointTransactionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PointHistoryDTO {
    Long id;
    Long customerId;
    Integer amountPoints;
    PointTransactionType type;
    String description;
    Instant createdDate;
}
