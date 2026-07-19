package com.djnd.cinema_java_spring.service.projection;

import java.math.BigDecimal;

public interface SalesChartProjection {
    String getDate();
    BigDecimal getRevenue();
    Long getTicketsCount();
}
