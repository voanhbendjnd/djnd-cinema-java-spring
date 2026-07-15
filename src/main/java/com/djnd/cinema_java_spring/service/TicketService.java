package com.djnd.cinema_java_spring.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.entity.BookingDetail;
import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.entity.Showtime;
import com.djnd.cinema_java_spring.domain.entity.Ticket;
import com.djnd.cinema_java_spring.repository.BookingRepository;
import com.djnd.cinema_java_spring.repository.TicketRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.TicketDTO;
import com.djnd.cinema_java_spring.service.realtime.SeatRealtime;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
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
    final BookingRepository bookingRepository;
    final SeatRealtime seatRealtime;

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
                    .roomName(showtime.getRoom().getName())
                    .seatPosition(seatPosition)
                    .price(ticket.getPrice())
                    .build();
        }).toList());

        return res;
    }

    public TicketDTO getDetailTicketCustomer(Long ticketId) {
        Long customerId = SecurityUtils.getCurrentUserIdOrNull();
        if (customerId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        Ticket ticket = ticketRepository.getTicketWithDetailByCustomerIdAndId(customerId, ticketId)
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
                .roomName(showtime.getRoom().getName())
                .build();
    }

    public void checkTicketWithSeatSold(Long showtimeId, List<Integer> seatIds, List<String> errorMessages) {
        List<String> positionSeats = ticketRepository.getSeatsPositionSold(showtimeId, seatIds);
        if (positionSeats != null && positionSeats.isEmpty()) {
            for (String position : positionSeats) {
                errorMessages.add("Already ticket with seat [" + position + "]");
            }
        }

    }

    public List<TicketDTO> getTicketByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));
        List<TicketDTO> res = new ArrayList<>();
        List<Ticket> tickets = ticketRepository.getTicketsByBookingId(bookingId);

        for (Ticket ticket : tickets) {
            TicketDTO ticketDTO = new TicketDTO();
            Showtime showtime = ticket.getShowtime();
            ticketDTO.setBookingAt(LocalDateTime.ofInstant(ticket.getCreatedDate(), ZoneOffset.systemDefault()));
            ticketDTO.setId(ticket.getId());
            ticketDTO.setSeatPosition(ticket.getSeat().getSeatRow() + ticket.getSeat().getSeatNo());
            ticketDTO.setStartDateTime(showtime.getStartDateTime().toLocalTime());
            ticketDTO.setEndDateTime(showtime.getEndDateTime().toLocalTime());
            ticketDTO.setPrice(ticket.getPrice());
            ticketDTO.setCreatedBy(ticket.getCreatedBy());
            ticketDTO.setMovieTitle(showtime.getMovie().getTitle());
            ticketDTO.setPaymentMethod(booking.getPaymentMethod());
            ticketDTO.setReleaseDate(showtime.getStartDateTime().toLocalDate());
            ticketDTO.setSeatType(ticket.getSeat().getType());
            ticketDTO.setTicketCode(ticket.getCode());
            ticketDTO.setRoomName(showtime.getRoom().getName());
            res.add(ticketDTO);
        }
        return res;
    }

    @Transactional
    public List<Ticket> createTicketsWithBookingDetailsWhenPaymentBookingSuccess(Booking bookingExisting,
            List<Integer> seatIds, Long showtimeId) {
        if (bookingExisting != null && bookingExisting.getBookingDetails() != null) {
            if (ticketRepository.existByShowtimeIdAndSeatIdIn(showtimeId, seatIds)) {
                throw new RequestInvalidException("Duplicated ticket!");
            }
            List<Ticket> saveTickets = new ArrayList<>();
            for (BookingDetail detail : bookingExisting.getBookingDetails()) {
                Ticket newTicket = new Ticket();
                newTicket.setBooking(bookingExisting);
                newTicket.setSeat(detail.getSeat());
                newTicket.setShowtime(detail.getShowtime());
                newTicket.setPrice(detail.getPrice());
                newTicket.setCode(UUID.randomUUID() + "");
                saveTickets.add(newTicket);
            }
            try {
                saveTickets = ticketRepository.saveAll(saveTickets);
                seatRealtime.sendSeatSold(showtimeId, seatIds);
                return saveTickets;

            } catch (DataIntegrityViolationException ex) {
                throw new RequestInvalidException("Seat had already exist ticket!");
            }
        }

        throw new RequestInvalidException("Booking not found!");
    }


    public void deleteTicketForCustomerChangeTicketToPoint(Long ticketId){
        this.ticketRepository.deleteById(ticketId);
    }

}
