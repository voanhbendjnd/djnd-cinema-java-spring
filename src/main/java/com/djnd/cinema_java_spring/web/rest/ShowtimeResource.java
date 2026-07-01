package com.djnd.cinema_java_spring.web.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.SeatService;
import com.djnd.cinema_java_spring.service.ShowtimeService;
import com.djnd.cinema_java_spring.service.dto.MovieRoomTimeDTORequest;
import com.djnd.cinema_java_spring.service.dto.ResSeatAtRoomBookingDTO;
import com.djnd.cinema_java_spring.service.projection.ShowtimeProjection;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.vm.ShowtimeVM;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowtimeResource {
    final ShowtimeService showtimeService;
    final SeatService seatService;

    @PostMapping("/admin/showtimes/check")
    @ApiMessage("Check conflict showtime at the room")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<Void> checkConflictShowtimeAtRoom(@Valid @RequestBody MovieRoomTimeDTORequest dto) {
        showtimeService.checkScheduleConflictAtRoom(dto);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/admin/showtimes")
    @ApiMessage("Get all start date time")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<List<ShowtimeProjection>> getAllStartDateTimeAtDateByRoom(@Valid @RequestBody ShowtimeVM vm) {
        return ResponseEntity
                .ok(showtimeService.getAllScheduleRoomAndMovieTitle(vm.getRoomId(), vm.getDate(), vm.getMovieId()));
    }

    @GetMapping("/showtimes/{id}/seats")
    @ApiMessage("Get seat layout by showtime")
    public ResponseEntity<ResSeatAtRoomBookingDTO> getSeatLayoutForBooking(
            @Positive @PathVariable("id") Long showtimeId) {
        return ResponseEntity.ok(seatService.getSeatAtRoomLayoutByShowtime(showtimeId));
    }
}
