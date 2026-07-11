package com.djnd.cinema_java_spring.web.rest.vm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherCursorVM {
    Integer size;
    LocalDateTime cursor;
    Long voucherId;
}
