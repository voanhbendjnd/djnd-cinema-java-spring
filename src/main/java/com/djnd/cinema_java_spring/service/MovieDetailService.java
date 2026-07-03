package com.djnd.cinema_java_spring.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Movie;
import com.djnd.cinema_java_spring.repository.MovieRepository;
import com.djnd.cinema_java_spring.service.dto.MovieDetailsResponse;
import com.djnd.cinema_java_spring.service.dto.ShowTimeResponse;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MovieDetailService {

        private final MovieRepository movieRepository;

        @Transactional(readOnly = true)
        public MovieDetailsResponse getMovieDetails(Integer movieId) {
                Movie movie = movieRepository.findDetailById(movieId)
                                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

                List<ShowTimeResponse> showtimes = movie.getShowtimes()
                                .stream().map(
                                                (showtime) -> new ShowTimeResponse(showtime.getId(),
                                                                showtime.getStartDateTime(), showtime.getEndDateTime(),
                                                                showtime.getRoom().getId(),
                                                                showtime.getRoom().getName(),
                                                                showtime.getRoom().getType()))
                                .toList();

                return new MovieDetailsResponse(
                                movie.getId(),
                                movie.getTitle(),
                                movie.getDescription(),
                                movie.getDurationMinutes(),
                                movie.getGenre(),
                                movie.getReleaseDate(),
                                movie.getPosterUrl(),
                                movie.getDirector(),
                                movie.getStatus(),
                                showtimes);

        }

}
