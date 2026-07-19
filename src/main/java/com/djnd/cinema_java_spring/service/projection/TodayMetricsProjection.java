package com.djnd.cinema_java_spring.service.projection;

import java.math.BigDecimal;

public interface TodayMetricsProjection {
    BigDecimal getTotalRevenue();
    Long getTicketsSold();
    Long getNewBookings();
}
