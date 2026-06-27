package com.djnd.cinema_java_spring.web.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.domain.enumeration.SeatType;
import com.djnd.cinema_java_spring.repository.ShowtimePriceRepository;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.ShowtimePriceService;
import com.djnd.cinema_java_spring.service.dto.ShowtimePriceDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/admin/showtime-price")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ShowtimePriceResource {
    final ShowtimePriceService showtimePriceService;
    final ShowtimePriceRepository showtimePriceRepository;

    private void validDataRequest(ShowtimePriceDTO dto) {
        if (dto.getStartTimeFrom().isAfter(dto.getStartTimeTo())) {
            throw new RequestInvalidException("Start time from must be is before start time to!");
        }
        try {
            SeatType.valueOf(dto.getSeatType());
        } catch (Exception ex) {
            throw new RequestInvalidException("Seat type invalid!");
        }
    }

    @PostMapping
    @ApiMessage("Init price for seat for day of week")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<ShowtimePriceDTO> createPriceForSeat(@Valid @RequestBody ShowtimePriceDTO dto) {
        if (dto.getId() != null) {
            throw new RequestInvalidException("A new object create cannot include ID!");
        }
        validDataRequest(dto);
        if (showtimePriceRepository.existsByOverlapTime(dto.getDayType(), SeatType.valueOf(dto.getSeatType()),
                dto.getStartTimeFrom(), dto.getStartTimeFrom())) {
            throw new RequestInvalidException("Time overlaps");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(showtimePriceService.createShowtimePrice(dto));
    }

    @PutMapping
    @ApiMessage("Update price")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<ShowtimePriceDTO> updatePrice(@Valid @RequestBody ShowtimePriceDTO dto) {
        if (dto.getId() == null) {
            throw new RequestInvalidException("Missing ID request for server!");
        }
        validDataRequest(dto);
        if (showtimePriceRepository.existsByOverlapTimeAndIdNot(dto.getId(), dto.getDayType(),
                SeatType.valueOf(dto.getSeatType()),
                dto.getStartTimeFrom(), dto.getStartTimeFrom())) {
            throw new RequestInvalidException("Time overlaps");
        }
        return ResponseEntity.ok(showtimePriceService.updateShowtimePrice(dto));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete showtime price")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<Void> delete(@Positive @PathVariable("id") Integer id) {
        showtimePriceService.deleteShowtimePrice(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping
    @ApiMessage("Get all price showtime")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<List<ShowtimePriceDTO>> getAll() {
        return ResponseEntity.ok(showtimePriceService.getAllShowtimePrice());
    }

}
