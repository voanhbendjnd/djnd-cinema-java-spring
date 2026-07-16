package com.djnd.cinema_java_spring.web.rest;

import com.djnd.cinema_java_spring.domain.entity.SeatMaintenance;
import com.djnd.cinema_java_spring.repository.SeatMaintenanceRepository;
import com.djnd.cinema_java_spring.service.MailService;
import com.djnd.cinema_java_spring.service.SeatMaintenanceService;
import com.djnd.cinema_java_spring.service.SeatService;
import com.djnd.cinema_java_spring.service.dto.SeatMaintenanceDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SeatMaintenanceResource {
    final SeatMaintenanceService seatMaintenanceService;

    @PostMapping
    @ApiMessage("Main seat by staff")
    public ResponseEntity<Void> maintenanceSeat(@Valid @RequestBody SeatMaintenanceDTO seatMaintenanceDTO) {
        if(seatMaintenanceDTO.getEndTime().isBefore(seatMaintenanceDTO.getStartTime())) {
            throw new RequestInvalidException("Start time must be after end time!");
        }
        if(seatMaintenanceDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RequestInvalidException("Start time must be is after current!");
        }
        seatMaintenanceService.handleSeatWhenNeedMaintenance(seatMaintenanceDTO);
        return ResponseEntity.ok(null);
    }
}
