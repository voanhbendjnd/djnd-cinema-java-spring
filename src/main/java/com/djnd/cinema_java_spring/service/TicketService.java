package com.djnd.cinema_java_spring.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.entity.Showtime;
import com.djnd.cinema_java_spring.domain.entity.Ticket;
import com.djnd.cinema_java_spring.repository.TicketRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.TicketDTO;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TicketService {
    final TicketRepository ticketRepository;

    public ResultPaginationDTO getAllTicketWithCustomer(Pageable pageable) {
        Long customerId = SecurityUtils.getCurrentUserIdOrNull();
        if (customerId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        Page<Ticket> page = ticketRepository.getTicketsWithCustomerId(customerId, pageable);
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(ticket -> {
            LocalDateTime bookingAt = LocalDateTime.ofInstant(ticket.getCreatedDate(),
                    ZoneOffset.systemDefault());
            Showtime showtime = ticket.getShowtime();
            LocalTime startDateTime = showtime.getStartDateTime().toLocalTime();
            LocalTime enddLocalTime = showtime.getEndDateTime().toLocalTime();
            String movieTitle = showtime.getMovie().getTitle();
            Seat seat = ticket.getSeat();
            String seatPosition = seat.getSeatRow() + seat.getSeatNo();
            return TicketDTO.builder()
                    .id(ticket.getId())
                    .bookingAt(bookingAt)
                    .startDateTime(startDateTime)
                    .endDateTime(enddLocalTime)
                    .releaseDate(showtime.getStartDateTime().toLocalDate())
                    .movieTitle(movieTitle)
                    .seatType(seat.getType())
                    .seatPosition(seatPosition)
                    .build();
        }).toList());

        return res;
    }

    public TicketDTO getDetailTicketCustomer(Long ticketId) {
        Long customerId = SecurityUtils.getCurrentUserIdOrNull();
        if (customerId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        Ticket ticket = ticketRepository.getTickeWithDetailByCustomerIdAndId(customerId, ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found!"));
        LocalDateTime bookingAt = LocalDateTime.ofInstant(ticket.getCreatedDate(), ZoneOffset.systemDefault());
        Showtime showtime = ticket.getShowtime();
        LocalTime startDateTime = showtime.getStartDateTime().toLocalTime();
        LocalTime enddLocalTime = showtime.getEndDateTime().toLocalTime();
        String movieTitle = showtime.getMovie().getTitle();
        Seat seat = ticket.getSeat();
        String seatPosition = seat.getSeatRow() + seat.getSeatNo();
        return TicketDTO.builder()
                .id(ticket.getId())
                .bookingAt(bookingAt)
                .startDateTime(startDateTime)
                .endDateTime(enddLocalTime)
                .movieTitle(movieTitle)
                .seatType(seat.getType())
                .seatPosition(seatPosition)
                .releaseDate(showtime.getStartDateTime().toLocalDate())
                .paymentMethod(ticket.getBooking().getPaymentMethod())
                .createdBy(ticket.getCreatedBy())
                .ticketCode(ticket.getCode())
                .price(ticket.getPrice())
                .build();
    }

}
