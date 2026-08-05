package com.djnd.cinema_java_spring.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.service.dto.MovieSuggestionDTO;
import com.djnd.cinema_java_spring.domain.enumeration.MovieStatus;
import com.djnd.cinema_java_spring.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieFallbackService {

    private final MovieRepository movieRepository;

    public List<MovieSuggestionDTO> getTopMovies(int limit) {
        return movieRepository
                .findTopSellingMovies(MovieStatus.SHOWING, PageRequest.of(0, limit))
                .stream()
                .map(m -> new MovieSuggestionDTO(
                        m.getId(),
                        m.getTitle(),
                        m.getGenre() != null ? m.getGenre().name() : null))
                .toList();
    }
}
