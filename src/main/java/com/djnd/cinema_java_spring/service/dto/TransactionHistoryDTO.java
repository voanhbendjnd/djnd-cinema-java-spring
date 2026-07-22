package com.djnd.cinema_java_spring.service.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionHistoryDTO {
    Long id;
    Long ticketId;
    String action;
    String reason;
    BigDecimal amount;
}
