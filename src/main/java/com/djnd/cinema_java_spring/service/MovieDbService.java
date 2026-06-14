package com.djnd.cinema_java_spring.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Movie;
import com.djnd.cinema_java_spring.domain.enumeration.MovieGenre;
import com.djnd.cinema_java_spring.domain.enumeration.MovieStatus;
import com.djnd.cinema_java_spring.repository.MovieRepository;
import com.djnd.cinema_java_spring.service.dto.AdminMovieDTO;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class MovieDbService {
    final MovieRepository movieRepository;

    public Movie saveMovie(AdminMovieDTO movieDTO) {
        Movie movie = Movie.builder()
                .description(movieDTO.getDescription())
                .director(movieDTO.getDirector())
                .durationMinutes(movieDTO.getDurationMinutes())
                .genre(MovieGenre.valueOf(movieDTO.getGenre()))
                .releaseDate(movieDTO.getReleaseDate())
                .status(MovieStatus.valueOf(movieDTO.getStatus()))
                .title(movieDTO.getTitle())
                .posterUrl(movieDTO.getPosterUrl())
                .build();
        return movieRepository.save(movie);
    }

}
