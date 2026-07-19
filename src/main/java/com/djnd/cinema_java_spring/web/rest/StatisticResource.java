package com.djnd.cinema_java_spring.web.rest;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.StatisticService;
import com.djnd.cinema_java_spring.service.dto.ResStatisticMetricDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ResStatisticMetricDTO> getStatistics(){
        return ResponseEntity.ok(statisticService.getSalesChartMetricsPublish());
    }
}
