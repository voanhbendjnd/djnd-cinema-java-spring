package com.djnd.cinema_java_spring.service.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultPaginationVoucherCursor extends ResultPaginationDTO {
    LocalDateTime nextCursor;
    Long voucherId;
    boolean hasMore;
}
