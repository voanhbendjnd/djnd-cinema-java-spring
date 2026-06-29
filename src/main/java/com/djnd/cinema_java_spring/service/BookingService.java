package com.djnd.cinema_java_spring.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.entity.Showtime;
import com.djnd.cinema_java_spring.domain.entity.ShowtimePriceMatrix;
import com.djnd.cinema_java_spring.domain.entity.Ticket;
import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;
import com.djnd.cinema_java_spring.repository.BookingRepository;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.SeatRepository;
import com.djnd.cinema_java_spring.repository.ShowtimePriceRepository;
import com.djnd.cinema_java_spring.repository.ShowtimeRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.BookingRequestDTO;
import com.djnd.cinema_java_spring.service.dto.ResBookingDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.SeatOccupiedException;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tech.jhipster.security.RandomUtil;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingService {
    final BookingRepository bookingRepository;
    final ShowtimeRepository showtimeRepository;
    final ShowtimePriceRepository showtimePriceRepository;
    final SeatRepository seatRepository;
    final CustomerRepository customerRepository;
    final VNPayService vnPayService;
    final TicketRepository ticketRepository;
    final StringRedisTemplate redisTemplate;
    private static final String EXPIRE_TIME_HOLDING_SEATS = "600"; // 10 minutes
    static final String LUA_HOLD_SEATS_AT_SHOWTIME = "local showtimeKey = KEYS[1] " +
            "local expireTime = tonumber(ARGV[1]) " +
            "local occupiedSeats = {} " +
            "for i = 2, #ARGV do " +
            "   if redis.call('HEXISTS', showtimeKey, ARGV[i]) == 1 then " +
            "       table.insert(occupiedSeats, ARGV[i]) " +
            "   end " +
            "end " +
            "if #occupiedSeats == 0 then " +
            "   for i = 2, #ARGV do " +
            "       redis.call('HSET', showtimeKey, ARGV[i], 'HOLDING') " +
            "   end " +
            "   redis.call('EXPIRE', showtimeKey, expireTime) " +
            "   return {} " +
            "else " +
            "   return occupiedSeats " +
            "end";

    public ResBookingDTO createBooking(BookingRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found customer!"));
        // start check seats available turn 1
        String showtimeKeyRedis = "showtime:" + request.getShowtimeId() + ":seats";
        List<String> argsRedis = new ArrayList<>();
        argsRedis.add(EXPIRE_TIME_HOLDING_SEATS);
        request.getSeatIds().forEach(seatId -> argsRedis.add(String.valueOf(seatId)));
        DefaultRedisScript<List<String>> redisScript = new DefaultRedisScript<>(LUA_HOLD_SEATS_AT_SHOWTIME);
        List<String> occupiedSeatIds = redisTemplate.execute(redisScript,
                Collections.singletonList(showtimeKeyRedis),
                argsRedis.toArray());
        if (occupiedSeatIds != null && !occupiedSeatIds.isEmpty()) {
            List<Integer> errorOccupiedSeatIds = occupiedSeatIds.stream().map(Integer::parseInt).toList();
            throw new SeatOccupiedException("Some seat are already holding or sold!", errorOccupiedSeatIds);
        }
        // end check
        List<Seat> seats = seatRepository.findByIdIn(request.getSeatIds());
        List<Integer> seatIdAvaliables = seats.stream().map(Seat::getId).toList();
        List<String> errorMessages = new ArrayList<>();
        for (Integer seatId : request.getSeatIds()) {
            if (!seatIdAvaliables.contains(seatId)) {
                errorMessages.add("Seat with ID " + seatId + " not found!");
            }
        }
        if (!errorMessages.isEmpty()) {
            this.rollbackRedisSeats(showtimeKeyRedis, request.getSeatIds());
            throw new ResourceNotFoundException(String.join("\n", errorMessages));
        }
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found!"));
        List<Ticket> availableTickets = ticketRepository.findByShowtimeId(showtime.getId());
        List<Integer> availableSeatTickets = availableTickets.stream().map(x -> x.getSeat().getId()).toList();
        for (Integer seatId : seatIdAvaliables) {
            if (availableSeatTickets.contains(seatId)) {
                errorMessages.add(String.format("Seat with ID %d already exist!", seatId));
            }
        }
        if (!errorMessages.isEmpty()) {
            this.rollbackRedisSeats(showtimeKeyRedis, request.getSeatIds());

            throw new RequestInvalidException(String.join("\n", errorMessages));
        }
        String dayType = (showtime.getStartDateTime().getDayOfWeek().getValue() >= 5) ? "WEEKEND" : "WEEKDAY";

        LocalTime showtimeAt = showtime.getStartDateTime().toLocalTime();
        List<ShowtimePriceMatrix> priceMatrixList = showtimePriceRepository.findByDayAndStartTime(dayType,
                showtimeAt);
        Map<SeatType, BigDecimal> priceSeatMap = priceMatrixList.stream()
                .collect(Collectors.toMap(ShowtimePriceMatrix::getSeatType, ShowtimePriceMatrix::getFinalPrice));
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Ticket> saveTickets = new ArrayList<>();
        Booking booking = new Booking();
        for (Seat seat : seats) {
            BigDecimal costSeat = priceSeatMap.get(seat.getType());
            if (costSeat == null) {
                errorMessages.add("Cannot get price config for seat: " + seat.getSeatRow() + seat.getSeatNo()
                        + " [Type: " + seat.getType() + "]");
            } else {
                totalAmount = totalAmount.add(costSeat);
                Ticket ticket = new Ticket();
                ticket.setPrice(costSeat);
                ticket.setSeat(seat);
                ticket.setShowtime(showtime);
                saveTickets.add(ticket);
            }
        }
        if (!errorMessages.isEmpty()) {
            this.rollbackRedisSeats(showtimeKeyRedis, request.getSeatIds());
            throw new ResourceNotFoundException(String.join("\n", errorMessages));
        }
        String dateStr = LocalDate.now().toString().replace("-", "");
        booking.setBookingCode(
                "BK-" + dateStr + "-" + RandomUtil.generateRandomAlphanumericString() + UUID.randomUUID());

        booking.setCustomer(customer);
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(totalAmount);
        for (Ticket ticket : saveTickets) {
            ticket.setBooking(booking);
        }
        booking.setTickets(saveTickets);
        try {
            booking = bookingRepository.save(booking);
            // already config cascade below code unnessecsary
            // ticketRepository.saveAll(saveTickets);
            bookingRepository.flush();
            String paymentUrl = vnPayService.createPaymentUrl(booking.getId(), totalAmount);
            ResBookingDTO res = new ResBookingDTO();
            res.setBookingId(booking.getId());
            res.setPaymentUrl(paymentUrl);
            return res;

        } catch (Exception ex) {
            this.rollbackRedisSeats(showtimeKeyRedis, request.getSeatIds());

            if (ex instanceof DataIntegrityViolationException) {
                throw new RequestInvalidException(
                        "The system is busy because someone else has booked the same seat as you. Please try again!");
            }
            throw ex;

        }

    }

    private void rollbackRedisSeats(String redisKey, List<Integer> seatIds) {
        try {
            Object[] fields = seatIds.stream().map(String::valueOf).toArray();
            redisTemplate.opsForHash().delete(redisKey, fields);
        } catch (Exception ex) {
            log.error("Failed to rollback locked seats in Redis for key: {}", redisKey);
        }
    }

}
