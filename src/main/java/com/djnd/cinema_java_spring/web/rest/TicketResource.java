package com.djnd.cinema_java_spring.web.rest;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.TicketService;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.TicketDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TicketResource {
    final TicketService ticketService;

    @GetMapping("/tickets")
    @ApiMessage("Get all ticket of customer with pagination")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.CUSTOMER + "\")")
    public ResponseEntity<ResultPaginationDTO> getAllTicketWithCustomer(Pageable pageable) {
        return ResponseEntity.ok(ticketService.getAllTicketWithCustomer(pageable));
    }

    @GetMapping("/tickets/{id}")
    @ApiMessage("Get ticket detail with customer")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.CUSTOMER + "\")")
    public ResponseEntity<TicketDTO> getTicketDetailWithCustomer(@Positive @PathVariable("id") Long ticketId) {
        return ResponseEntity.ok(ticketService.getDetailTicketCustomer(ticketId));
    }
}
