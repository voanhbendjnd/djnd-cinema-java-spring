package com.djnd.cinema_java_spring.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.djnd.cinema_java_spring.domain.enumeration.MovieGenre;
import com.djnd.cinema_java_spring.domain.enumeration.MovieStatus;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.RoomService;
import com.djnd.cinema_java_spring.service.ShowtimeService;
import com.djnd.cinema_java_spring.service.dto.AdminMovieDTO;
import com.djnd.cinema_java_spring.service.dto.ComplexShowtimeRequestDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.ShowtimeDTO;
import com.djnd.cinema_java_spring.service.facade.MovieFacadeService;
import com.djnd.cinema_java_spring.service.projection.RoomNameProjection;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MovieResource {
    final MovieFacadeService movieFacadeService;
    final RoomService roomService;
    final ShowtimeService showtimeService;

    private boolean isValidFileImage(MultipartFile file) {
        var fileName = file.getOriginalFilename();
        if (fileName != null) {
            var listAllowed = List.of(".png", ".jpeg", ".jpg", ".webp");
            return listAllowed.stream().anyMatch(x -> fileName.endsWith(x.toLowerCase()));
        }
        return false;
    }

    private void validDataMovie(AdminMovieDTO movieDTO) {
        if (movieDTO.getStatus() != null) {
            try {
                MovieStatus.valueOf(movieDTO.getStatus());
            } catch (Exception ex) {
                throw new RequestInvalidException("Movie status invalid!");
            }
        }
        if (movieDTO.getGenre() != null) {
            try {
                MovieGenre.valueOf(movieDTO.getGenre());
            } catch (Exception ex) {
                throw new RequestInvalidException("Movie genre invalid!");
            }
        }
    }

    @GetMapping("/movies/{id}/showtimes")
    @ApiMessage("Get showtime by movie and day")
    public ResponseEntity<ShowtimeDTO> getShowtimeByMovieAndDay(@Positive @PathVariable("id") Integer movieId,
            @RequestParam(name = "date", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.getShowtimeActiveAtDay(movieId, date));
    }

    @PostMapping("/admin/movies/upload-temp")
    @ApiMessage("Upload file to temp")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<String> uploadTempFile(@RequestPart("file") MultipartFile file)
            throws URISyntaxException, IOException {
        if (!isValidFileImage(file)) {
            throw new IOException("File invalid!");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(movieFacadeService.saveTempFile(file));
    }

    @PostMapping("/admin/movies/")
    @ApiMessage("Create new movie")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<AdminMovieDTO> createMovie(@Valid @RequestBody ComplexShowtimeRequestDTO movieDTO)
            throws URISyntaxException, IOException {
        if (movieDTO.getId() != null) {
            throw new RequestInvalidException("A new movie cannot already have an ID!");
        }
        validDataMovie(movieDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(movieFacadeService.createMovie(movieDTO));
    }

    @GetMapping("/admin/movies/rooms")
    @ApiMessage("Get all room available for movie")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<List<RoomNameProjection>> getRoomsForMovie() {
        return ResponseEntity.ok(roomService.getAllRoomForInitMovie());
    }

    @PutMapping("/admin/movies")
    @ApiMessage("Update movie")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<AdminMovieDTO> updateMovie(@Valid @RequestBody ComplexShowtimeRequestDTO movieDTO)
            throws URISyntaxException, IOException {
        if (movieDTO.getId() == null) {
            throw new RequestInvalidException("Missing movie ID!");
        }
        validDataMovie(movieDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(movieFacadeService.updateMovie(movieDTO));
    }

    @GetMapping("/admin/movies")
    @ApiMessage("Fetch all movie with pagination")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<ResultPaginationDTO> fetchAllMovieWithPagination(
            @RequestParam(name = "q", required = true) String q, Pageable pageable) {
        return ResponseEntity.ok(movieFacadeService.getAllMovieWithPagination(pageable, q));
    }

    @GetMapping("/admin/movies/{id}")
    @ApiMessage("Fetch movie by Id")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<ComplexShowtimeRequestDTO> fetchMovieById(@Positive @PathVariable("id") Integer id) {
        if (id == null)
            throw new RequestInvalidException("Movie ID missing!");
        return ResponseEntity.ok(movieFacadeService.getMovieRoomsShowtimes(id));
    }
}
