package com.djnd.cinema_java_spring.service.projection;

import java.time.LocalDateTime;

import com.djnd.cinema_java_spring.domain.enumeration.MovieGenre;
import com.djnd.cinema_java_spring.domain.enumeration.MovieStatus;

public interface PublishMovieProjection {
    Integer getId();

    String getTitle();

    String getPosterUrl();

    LocalDateTime getReleaseDate();

    MovieGenre getGenre();

    Integer getSold();

    MovieStatus getStatus();
}
