package com.djnd.cinema_java_spring.web.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.service.facade.MovieFacadeService;
import com.djnd.cinema_java_spring.service.projection.PublishMovieProjection;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/home")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class HomeController {
    final MovieFacadeService movieFacadeService;

    @GetMapping("/movies")
    @ApiMessage("Get all movie for guest")
    public ResponseEntity<List<PublishMovieProjection>> getMoviePublish() {
        return ResponseEntity.ok(movieFacadeService.getAllMovieShowingPublish());
    }

    @GetMapping(value = "/movies", params = "status")
    @ApiMessage("Get all movie for guest")
    public ResponseEntity<List<PublishMovieProjection>> getMoviePublish(
            @RequestParam(name = "status", required = true) String status) {
        return ResponseEntity.ok(movieFacadeService.getPublishMoviesByStatus(status));
    }
}
