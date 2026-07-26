package com.djnd.cinema_java_spring.service.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface OccupancyProjection {
    Double getOverallOccupancyRate();
    Long getTotalTicketsSold();
    Long getTotalCapacity();
    List<Detail> details = List.of();
    interface Detail{
        Long getShowtimeId();
        String getMovieTitle();
        String getRoomName();
        LocalDate getShowDate();
        LocalTime getStartTime();
        Integer getRoomCapacity();
        Long getTicketsSold();
        Double getOccupancyRate();
        
    }
}
