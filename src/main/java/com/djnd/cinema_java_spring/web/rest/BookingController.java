package com.djnd.cinema_java_spring.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.domain.enumeration.PaymentMethod;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.BookingService;
import com.djnd.cinema_java_spring.service.dto.BookingRequestDTO;
import com.djnd.cinema_java_spring.service.dto.ResBookingDTO;
import com.djnd.cinema_java_spring.service.dto.UserDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;

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

    private void validRequestBooking(BookingRequestDTO requestDTO) {
        try {
            PaymentMethod.valueOf(requestDTO.getPaymentMethod());
        } catch (Exception ex) {
            throw new RequestInvalidException("Payment method invalid format!");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.CUSTOMER + "\")")
    public ResponseEntity<ResBookingDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {
        validRequestBooking(request);
        ResBookingDTO res = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/with-staff")
    @ApiMessage("Booking with staff")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.STAFF + "','" + AuthoritiesConstants.ADMIN + "', '"
            + AuthoritiesConstants.MANAGER + "')")

    public ResponseEntity<ResBookingDTO> createBookingAtStaff(@Valid @RequestBody BookingRequestDTO requestDTO) {
        validRequestBooking(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBookingByStaff(requestDTO));
    }

    @GetMapping("/customer-by-email")
    @ApiMessage("Get customer by email")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.STAFF + "','" + AuthoritiesConstants.ADMIN + "', '"
            + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<UserDTO> getCustomerByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(bookingService.getCustomerByEmail(email));
    }

}
