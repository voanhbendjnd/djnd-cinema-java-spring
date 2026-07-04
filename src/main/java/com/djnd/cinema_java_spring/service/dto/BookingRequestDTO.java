package com.djnd.cinema_java_spring.service.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestDTO {
    @NotNull
    Long showtimeId;
    @NotEmpty
    List<Integer> seatIds;
    @NotNull
    String paymentMethod;
    Long customerId;
    Boolean isNotMember;

}
