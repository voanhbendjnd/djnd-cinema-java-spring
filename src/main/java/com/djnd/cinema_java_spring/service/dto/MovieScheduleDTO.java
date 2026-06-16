package com.djnd.cinema_java_spring.service.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieScheduleDTO extends AdminMovieDTO implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;
    Integer roomId;
    LocalDateTime starDateTime;
    LocalDateTime endDateTime;
    String showTimeStatus;
    BigDecimal ticketPrice;

}
