package com.djnd.cinema_java_spring.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.BookingService;
import com.djnd.cinema_java_spring.service.dto.BookingRequestDTO;
import com.djnd.cinema_java_spring.service.dto.ResBookingDTO;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingController {
    final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.CUSTOMER + "\")")
    public ResponseEntity<ResBookingDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {
        ResBookingDTO res = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

}
