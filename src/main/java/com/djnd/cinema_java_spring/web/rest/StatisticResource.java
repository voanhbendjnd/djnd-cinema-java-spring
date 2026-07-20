package com.djnd.cinema_java_spring.web.rest;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.StatisticService;
import com.djnd.cinema_java_spring.service.dto.ResStatisticMetricDTO;
import com.djnd.cinema_java_spring.service.projection.TopMovieProjection;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/statistics")
public class StatisticResource {
    final StatisticService statisticService;


    @GetMapping("/sales-sumary")
    @ApiMessage("Report statistic")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '"
            + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<ResStatisticMetricDTO> getStatistics(@RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate fromDate, @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate toDate, @RequestParam(name = "roomId", required = false) Integer roomId) {
        if(fromDate == null){
            fromDate = LocalDate.now().minusDays(7);
        }
        if(toDate == null){
            toDate = LocalDate.now();
        }

        return ResponseEntity.ok(statisticService.getSalesChartMetricsPublish(fromDate, toDate, roomId));
    }

    @GetMapping("/top-movies")
    @ApiMessage("Show top movie by total revenue")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '"
            + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<List<TopMovieProjection>> getTopMovies(@RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate fromDate, @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate toDate, @RequestParam(name = "limit", required = false) Integer limit) {
        if(fromDate == null){
            fromDate = LocalDate.now().minusDays(7);
        }
        if(toDate == null){
            toDate = LocalDate.now();
        }
        if(limit == null){
            limit = 10;
        }

        return ResponseEntity.ok(statisticService.getTopMovieStatistics(fromDate, toDate, limit));
    }
}
