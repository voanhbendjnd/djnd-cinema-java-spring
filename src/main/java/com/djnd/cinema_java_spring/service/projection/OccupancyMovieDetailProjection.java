package com.djnd.cinema_java_spring.service.projection;

import java.math.BigDecimal;

public interface OccupancyMovieDetailProjection {
    Integer getMovieId();
    String getMovieTitle();
    BigDecimal getTotalRevenue();
    Long getTicketsSold();
    Long getTotalShowtimes();
    String getPosterUrl();
    Double getOccupancyRate();
}
