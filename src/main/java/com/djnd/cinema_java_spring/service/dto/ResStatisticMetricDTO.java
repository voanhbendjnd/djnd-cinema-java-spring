package com.djnd.cinema_java_spring.service.dto;

import com.djnd.cinema_java_spring.service.projection.SalesChartProjection;
import com.djnd.cinema_java_spring.service.projection.TodayMetricsProjection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResStatisticMetricDTO {
    TodayMetricsProjection todayMetrics;
    List<SalesChartProjection> chartData;
}
