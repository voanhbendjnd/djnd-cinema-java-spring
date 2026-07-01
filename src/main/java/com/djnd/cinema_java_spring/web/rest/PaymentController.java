package com.djnd.cinema_java_spring.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.BookingService;
import com.djnd.cinema_java_spring.service.dto.VNPayRequestDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentController {
    final BookingService bookingService;

    @GetMapping("/vnpay-ipn")
    @ApiMessage("Vnpay return server")
    public ResponseEntity<Void> handleVNPayIPN(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(null);
    }
    
    @GetMapping("/vnpay-return")
    @ApiMessage("Vnpay return server")
    public ResponseEntity<Void> handleVNPayReturn(@ModelAttribute VNPayRequestDTO requestDTO) {
        bookingService.processVNPayCallback(requestDTO);
        return ResponseEntity.ok(null);
    }
}
