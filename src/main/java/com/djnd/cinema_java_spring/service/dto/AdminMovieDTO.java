package com.djnd.cinema_java_spring.service.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminMovieDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Integer id;
    @NotBlank(message = "Title empty!")
    String title;
    String description;
    @NotNull(message = "Duration minutes not found!")
    @Min(value = 1, message = "Duration minutes must be greater 0")
    Integer durationMinutes;
    String genre;
    String director;
    LocalDateTime releaseDate;
    String posterUrl;
    String status;
}
