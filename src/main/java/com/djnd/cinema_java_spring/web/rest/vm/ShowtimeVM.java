package com.djnd.cinema_java_spring.web.rest.vm;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowtimeVM {
    @NotNull(message = "Room ID not found!")
    Integer roomId;
    @NotNull(message = "Room date not found!")
    LocalDate date;
}
