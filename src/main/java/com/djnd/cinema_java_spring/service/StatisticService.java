package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.repository.BookingRepository;
import com.djnd.cinema_java_spring.service.dto.ResStatisticMetricDTO;
import com.djnd.cinema_java_spring.service.projection.SalesChartProjection;
import com.djnd.cinema_java_spring.service.projection.TodayMetricsProjection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class StatisticService {
    final BookingRepository bookingRepository;
    // 7 days and metric today
    public ResStatisticMetricDTO getSalesChartMetricsPublish(){
        TodayMetricsProjection todayMetrics = bookingRepository.getTodayRevenue();
        List<SalesChartProjection> chartData = bookingRepository.getSalesChartMetrics();
        ResStatisticMetricDTO res = new ResStatisticMetricDTO();
        res.setChartData(chartData);
        res.setTodayMetrics(todayMetrics);
        return res;
    }
}
