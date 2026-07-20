package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.repository.BookingRepository;
import com.djnd.cinema_java_spring.repository.MovieRepository;
import com.djnd.cinema_java_spring.service.dto.ResStatisticMetricDTO;
import com.djnd.cinema_java_spring.service.projection.SalesChartProjection;
import com.djnd.cinema_java_spring.service.projection.TodayMetricsProjection;
import com.djnd.cinema_java_spring.service.projection.TopMovieProjection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class StatisticService {
    final BookingRepository bookingRepository;
    final MovieRepository movieRepository;
    // 7 days and metric today
    public ResStatisticMetricDTO getSalesChartMetricsPublish(LocalDate fromDate, LocalDate toDate,Integer roomId) {
        TodayMetricsProjection todayMetrics = bookingRepository.getTodayRevenue();
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atStartOfDay().plusDays(1);
        List<SalesChartProjection> chartData = bookingRepository.getSalesChartMetrics(fromDateTime, toDateTime, roomId);
        ResStatisticMetricDTO res = new ResStatisticMetricDTO();
        res.setChartData(chartData);
        res.setTodayMetrics(todayMetrics);
        return res;
    }

    /*
    * Get top movie
    * */
    public List<TopMovieProjection> getTopMovieStatistics(LocalDate fromDate, LocalDate toDate, int limit) {
        return movieRepository.getTopPerformingMovies(fromDate.atStartOfDay(), toDate.atStartOfDay().plusDays(1), limit);
    }
}
