package com.djnd.cinema_java_spring.service.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ComplexShowtimeRequestDTO extends AdminMovieDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    List<RoomScheduleDTO> rooms;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RoomScheduleDTO {
        Integer id;
        String name;
        List<DayScheduleDTO> days;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DayScheduleDTO {
        LocalDate date;
        List<LocalTime> startTimes;
    }
}
