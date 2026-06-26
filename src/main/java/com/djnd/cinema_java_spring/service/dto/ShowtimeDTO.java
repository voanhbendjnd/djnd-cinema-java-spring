package com.djnd.cinema_java_spring.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowtimeDTO {
    Integer movieId;
    List<Schedule> schedules;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Schedule {
        Long showtimeId;
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        Integer roomId;
    }
}
