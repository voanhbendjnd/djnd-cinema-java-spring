package com.djnd.cinema_java_spring.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.djnd.cinema_java_spring.domain.enumeration.MovieGenre;
import com.djnd.cinema_java_spring.domain.enumeration.MovieStatus;

public record MovieDetailsResponse(
                Integer id,
                String title,
                String description,
                Integer durationMunutes,
                MovieGenre genre,
                LocalDateTime releaseDate,
                String posterUrl,
                String director,
                MovieStatus movieStatus,
                List<ShowTimeResponse> showtimes) {

}
