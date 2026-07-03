package com.djnd.cinema_java_spring.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.service.MovieDetailService;
import com.djnd.cinema_java_spring.service.dto.MovieDetailsResponse;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieDetailController {

    private final MovieDetailService detailService;

    @GetMapping("/{movieId}")
    @ApiMessage("Get movie details")
    public ResponseEntity<MovieDetailsResponse> getMovieDetails(@PathVariable(name = "movieId") Integer movieId) {
        return ResponseEntity.ok(detailService.getMovieDetails(movieId));
    }

}
