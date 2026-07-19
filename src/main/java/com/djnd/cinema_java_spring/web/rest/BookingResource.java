package com.djnd.cinema_java_spring.web.rest;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.BookingService;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.projection.PublishCustomerBookingProjection;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BookingResource {
    final BookingService bookingService;

    @GetMapping("/bookings/publish")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.STAFF + "','" + AuthoritiesConstants.ADMIN + "', '"
            + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<ResultPaginationDTO> fetchAllBooking(@RequestParam(value = "q", required = true) String q,
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAllBookingWithPagination(pageable, q));
    }

    @GetMapping("/bookings/publish/{id}")
    @ApiMessage("Get booking detail")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.STAFF + "','" + AuthoritiesConstants.ADMIN + "', '"
            + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<PublishCustomerBookingProjection> getBookingDetail(@Positive @PathVariable("id") Long id) {
        if (id == null)
            throw new RequestInvalidException("Booking ID missing!");
        return ResponseEntity.ok(bookingService.getPublishBookingDetail(id));
    }


}
