package com.djnd.cinema_java_spring.service.facade;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.djnd.cinema_java_spring.domain.entity.Movie;
import com.djnd.cinema_java_spring.domain.enumeration.MovieGenre;
import com.djnd.cinema_java_spring.domain.enumeration.MovieStatus;
import com.djnd.cinema_java_spring.repository.MovieRepository;
import com.djnd.cinema_java_spring.repository.ShowtimeRepository;
import com.djnd.cinema_java_spring.service.FileService;
import com.djnd.cinema_java_spring.service.MovieDbService;
import com.djnd.cinema_java_spring.service.ShowtimeService;
import com.djnd.cinema_java_spring.service.dto.AdminMovieDTO;
import com.djnd.cinema_java_spring.service.dto.ComplexShowtimeRequestDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieFacadeService {
    final FileService fileService;
    final MovieDbService movieDbService;
    final MovieRepository movieRepository;
    final ShowtimeService showtimeService;
    final ShowtimeRepository showtimeRepository;

    @Transactional
    public AdminMovieDTO createMovie(ComplexShowtimeRequestDTO movieDTO) {
        if (movieDTO.getDurationMinutes() < 1) {
            throw new RequestInvalidException("Duration must be greater 0!");
        }
        if (movieDTO.getReleaseDate() != null) {
            if (movieDTO.getReleaseDate().isBefore(LocalDateTime.now())) {
                throw new RequestInvalidException("Release date is before current date!");
            }
        }
        Movie movieSaved = movieDbService.saveMovie(movieDTO);
        movieDTO.setId(movieSaved.getId());
        if (!movieDTO.getStatus().equals(MovieStatus.SHOWING.toString()) && movieDTO.getRooms() != null
                && !movieDTO.getRooms().isEmpty()) {
            throw new RequestInvalidException(
                    "Rooms and screenings can only be created after the movie has been shown!");
        }
        if (movieDTO.getStatus().equals(MovieStatus.SHOWING.toString()) && movieDTO.getRooms() != null) {
            showtimeService.createComplexShowtimes(movieDTO);
        }
        return this.toAdminMovieDTO(movieSaved);
    }

    @Transactional
    public AdminMovieDTO updateMovie(ComplexShowtimeRequestDTO movieDTO) {
        var movie = movieRepository.findById(movieDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found!"));
        if (movieDTO.getDurationMinutes() < 1) {
            throw new RequestInvalidException("Duration must be greater 0!");
        }
        boolean hasScreenings = showtimeRepository.movieHasHadScreenings(movieDTO.getId());
        if (hasScreenings) {
            LocalDateTime entityRelease = movie.getReleaseDate().withNano(0);
            LocalDateTime dtoRelease = movieDTO.getReleaseDate().withNano(0);

            if (!movie.getDurationMinutes().equals(movieDTO.getDurationMinutes())
                    || !entityRelease.isEqual(dtoRelease)) {
                throw new RequestInvalidException(
                        "It is not possible to change the film's length and release date if the film has already been scheduled for screenings!");
            }
        }
        if (!hasScreenings) {
            movie.setDurationMinutes(movieDTO.getDurationMinutes());
            movie.setReleaseDate(movieDTO.getReleaseDate());
        }
        movie.setDescription(movieDTO.getDescription());
        movie.setTitle(movieDTO.getTitle());
        movie.setDirector(movieDTO.getDirector());
        movie.setGenre(MovieGenre.valueOf(movieDTO.getGenre()));
        movie.setPosterUrl(movieDTO.getPosterUrl());
        movie.setStatus(MovieStatus.valueOf(movieDTO.getStatus()));
        movieDTO.setId(movie.getId());
        if (!movieDTO.getStatus().equals(MovieStatus.SHOWING.toString()) && movieDTO.getRooms() != null
                && !movieDTO.getRooms().isEmpty()) {
            throw new RequestInvalidException(
                    "Rooms and screenings can only be created after the movie has been shown!");
        }
        if (movieDTO.getStatus().equals(MovieStatus.SHOWING.toString()) && movieDTO.getRooms() != null) {

            showtimeService.updateComplexShowtimes(movieDTO);
        }
        return toAdminMovieDTO(movie);
    }

    public ResultPaginationDTO getAllMovieWithPagination(Pageable pageable, String q) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = movieRepository.fetchAllWithPagination(q, pageable);
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::toAdminMovieDTO).toList());
        return res;
    }

    public String saveTempFile(MultipartFile file) throws URISyntaxException, IOException {
        return fileService.getNameFileAtTemp(file);
    }

    public AdminMovieDTO fetchById(Integer id) {
        var movie = movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movie not found!"));
        return toAdminMovieDTO(movie);
    }

    public ComplexShowtimeRequestDTO getMovieRoomsShowtimes(Integer id) {
        var movie = movieRepository.findWithDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found!"));
        return showtimeService.toComplexShowtimeRequestDTO(movie);

    }

    private AdminMovieDTO toAdminMovieDTO(Movie movie) {
        return AdminMovieDTO.builder()
                .id(movie.getId())
                .description(movie.getDescription())
                .director(movie.getDirector())
                .durationMinutes(movie.getDurationMinutes())
                .genre(movie.getGenre().toString())
                .releaseDate(movie.getReleaseDate())
                .status(movie.getStatus().toString())
                .posterUrl(movie.getPosterUrl())
                .title(movie.getTitle()).build();
    }

}
