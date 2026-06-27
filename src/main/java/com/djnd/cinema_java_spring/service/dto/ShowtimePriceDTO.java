package com.djnd.cinema_java_spring.service.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowtimePriceDTO {
    Integer id;
    @Length(max = 20)
    @NotBlank
    String dayType;
    @NotBlank
    String seatType;
    @NotNull
    LocalTime startTimeFrom;
    @NotNull
    LocalTime startTimeTo;
    @NotNull
    BigDecimal finalPrice;
}
