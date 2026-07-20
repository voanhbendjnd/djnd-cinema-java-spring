package com.djnd.cinema_java_spring.service.projection;

import java.math.BigDecimal;

public interface TopMovieProjection {
    Integer getMovieId();
    String getMovieTitle();
    BigDecimal getTotalRevenue();
    Long getTicketsSold();
    Long getTotalShowtime();
    String getPosterUrl();
}
