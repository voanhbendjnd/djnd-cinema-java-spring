package com.djnd.cinema_java_spring.service.dto;

import com.djnd.cinema_java_spring.service.projection.OccupancyMovieDetailProjection;
import com.djnd.cinema_java_spring.service.projection.OccupancyProjection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OccupancyReportDTO {
    OccupancyProjection summary;
    List<OccupancyMovieDetailProjection> details;
}
