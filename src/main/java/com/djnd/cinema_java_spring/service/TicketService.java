package com.djnd.cinema_java_spring.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import com.djnd.cinema_java_spring.service.dto.TicketRefundInfoDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.entity.BookingDetail;
import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.Room;
import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.entity.Showtime;
import com.djnd.cinema_java_spring.domain.entity.Ticket;
import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;
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

    public TicketRefundInfoDTO getTicketRefundInfo(Long ticketId) {
        Ticket currentTicket = ticketRepository.getTicketRefund(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found!"));
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime startShowtimeMovie = currentTicket.getShowtime().getStartDateTime();
        if (currentDate.isBefore(startShowtimeMovie)) {
            throw new RequestInvalidException("Showtime already showing cannot operation!");
        }
        return TicketRefundInfoDTO.builder()
                .bookingCode(currentTicket.getBooking().getBookingCode())
                .seatPosition(currentTicket.getSeat().getSeatRow() + currentTicket.getSeat().getSeatNo())
                .customerEmail(currentTicket.getBooking().getCustomer().getUser().getEmail())
                .ticketCode(currentTicket.getCode())
                .originalAmount(currentTicket.getPrice())
                .refundAmount(currentTicket.getPrice())
                .movieTitle(currentTicket.getShowtime().getMovie().getTitle())
                .showtime(currentTicket.getShowtime().getStartDateTime())
                .ticketId(currentTicket.getId())
                .build();
    }

    private static final Pattern SEAT_POSITION_PATTERN = Pattern.compile("^([a-z]{1,3})0*([1-9][0-9]{0,2})$");

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
        res.setResult(page.getContent().stream().map(this::toTicketDTO).toList());

        return res;
    }

    public ResultPaginationDTO getAllTicketForAdmin(String q,
            SeatType seatType,
            String paymentMethod,
            BookingStatus bookingStatus,
            LocalDate releaseDate,
            Pageable pageable) {
        String exactSeatPosition = parseExactSeatPosition(q);
        String keyword = exactSeatPosition == null ? buildKeyword(q) : null;
        Long numericId = exactSeatPosition == null ? parseNumericId(q) : null;
        String normalizedPaymentMethod = normalizeBlankToNull(paymentMethod);
        LocalDateTime showDateStart = releaseDate != null ? releaseDate.atStartOfDay() : null;
        LocalDateTime showDateEnd = releaseDate != null ? releaseDate.plusDays(1).atStartOfDay() : null;

        Page<Ticket> page = ticketRepository.searchAdminTickets(keyword, exactSeatPosition, numericId, seatType,
                normalizedPaymentMethod, bookingStatus, showDateStart, showDateEnd, pageable);

        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::toTicketDTO).toList());
        return res;
    }

    public TicketDTO getDetailTicketCustomer(Long ticketId) {
        Long customerId = SecurityUtils.getCurrentUserIdOrNull();
        if (customerId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        Ticket ticket = ticketRepository.getTicketWithDetailByCustomerIdAndId(customerId, ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found!"));
        return toTicketDTO(ticket);
    }

    public void checkTicketWithSeatSold(Long showtimeId, List<Integer> seatIds, List<String> errorMessages) {
        List<String> positionSeats = ticketRepository.getSeatsPositionSold(showtimeId, seatIds);
        if (positionSeats != null && !positionSeats.isEmpty()) {
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
            res.add(toTicketDTO(ticket));
        }
        return res;
    }

    @Transactional
    public Map<Long, BigDecimal> createTicketsWithBookingDetailsWhenPaymentBookingSuccess(Booking bookingExisting,
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
                return saveTickets.stream().collect(Collectors.toMap(Ticket::getId, Ticket::getPrice));

            } catch (DataIntegrityViolationException ex) {
                throw new RequestInvalidException("Seat had already exist ticket!");
            }
        }

        throw new RequestInvalidException("Booking not found!");
    }

    @Transactional
    public void deleteOneTicket(Long ticketId) {
        this.ticketRepository.deleteById(ticketId);
    }

    @Transactional
    public void deleteTickets(List<Long> ticketIds) {
        ticketRepository.deleteAllById(ticketIds);
    }

    private TicketDTO toTicketDTO(Ticket ticket) {
        Showtime showtime = ticket.getShowtime();
        Seat seat = ticket.getSeat();
        Booking booking = ticket.getBooking();
        Customer customer = booking != null ? booking.getCustomer() : null;
        User customerUser = customer != null ? customer.getUser() : null;
        Room room = showtime != null ? showtime.getRoom() : null;
        LocalDateTime bookingAt = ticket.getCreatedDate() != null
                ? LocalDateTime.ofInstant(ticket.getCreatedDate(), ZoneOffset.systemDefault())
                : null;

        return TicketDTO.builder()
                .id(ticket.getId())
                .bookingAt(bookingAt)
                .showtime(showtime != null ? showtime.getStartDateTime() : null)
                .startDateTime(showtime != null ? showtime.getStartDateTime().toLocalTime() : null)
                .endDateTime(showtime != null ? showtime.getEndDateTime().toLocalTime() : null)
                .releaseDate(showtime != null ? showtime.getStartDateTime().toLocalDate() : null)
                .movieTitle(showtime != null && showtime.getMovie() != null ? showtime.getMovie().getTitle() : null)
                .seatType(seat != null ? seat.getType() : null)
                .seatPosition(seat != null ? seat.getSeatRow() + seat.getSeatNo() : null)
                .price(ticket.getPrice())
                .paymentMethod(booking != null ? booking.getPaymentMethod() : null)
                .createdBy(ticket.getCreatedBy())
                .ticketCode(ticket.getCode())
                .roomId(room != null ? room.getId() : null)
                .roomName(room != null ? room.getName() : null)
                .roomType(room != null && room.getType() != null ? room.getType().name() : null)
                .bookingId(booking != null ? booking.getId() : null)
                .bookingCode(booking != null ? booking.getBookingCode() : null)
                .bookingStatus(booking != null && booking.getStatus() != null ? booking.getStatus().name() : null)
                .customerId(customer != null ? customer.getUserId() : null)
                .customerLogin(customerUser != null ? customerUser.getLogin() : null)
                .customerName(customerUser != null ? customerUser.getName() : null)
                .customerPhone(customerUser != null ? customerUser.getPhone() : null)
                .customerIdentityCard(customer != null ? customer.getIdentityCard() : null)
                .build();
    }

    private String buildKeyword(String q) {
        String normalized = normalizeBlankToNull(q);
        return normalized != null ? "%" + normalized.toLowerCase() + "%" : null;
    }

    private Long parseNumericId(String q) {
        String normalized = normalizeBlankToNull(q);
        if (normalized == null || !normalized.matches("\\d+")) {
            return null;
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String parseExactSeatPosition(String q) {
        String normalized = normalizeBlankToNull(q);
        if (normalized == null) {
            return null;
        }

        String compact = normalized.replaceAll("\\s+", "").toLowerCase();
        var matcher = SEAT_POSITION_PATTERN.matcher(compact);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1) + Integer.parseInt(matcher.group(2));
    }

    private String normalizeBlankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

}
