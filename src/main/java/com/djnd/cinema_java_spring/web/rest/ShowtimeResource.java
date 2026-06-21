package com.djnd.cinema_java_spring.web.rest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.ShowtimeService;
import com.djnd.cinema_java_spring.service.dto.MovieRoomTimeDTORequest;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.vm.ShowtimeVM;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/admin/showtimes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowtimeResource {
    final ShowtimeService showtimeService;

    @PostMapping("/check")
    @ApiMessage("Check conflict showtime at the room")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<Void> checkConflictShowtimeAtRoom(@Valid @RequestBody MovieRoomTimeDTORequest dto) {
        showtimeService.checkScheduleConflictAtRoom(dto);
        return ResponseEntity.ok(null);
    }

    @PostMapping
    @ApiMessage("Get all start date time")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<List<LocalDateTime>> getAllStartDateTimeAtDateByRoom(@Valid @RequestBody ShowtimeVM vm) {
        return ResponseEntity.ok(showtimeService.getAllTimeAtDateByRoom(vm.getRoomId(), vm.getDate()));
    }
}
