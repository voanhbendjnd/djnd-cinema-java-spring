package com.djnd.cinema_java_spring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djnd.cinema_java_spring.domain.entity.*;
import com.djnd.cinema_java_spring.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.domain.enumeration.PaymentMethod;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.BookingRequestDTO;
import com.djnd.cinema_java_spring.service.dto.PublishBookingDTO;
import com.djnd.cinema_java_spring.service.dto.ResBookingDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
// import com.djnd.cinema_java_spring.service.dto.TicketDTO;
// import com.djnd.cinema_java_spring.service.dto.TicketMailEvent;
import com.djnd.cinema_java_spring.service.dto.UserDTO;
// import com.djnd.cinema_java_spring.service.producer.TicketEventProducer;
import com.djnd.cinema_java_spring.service.projection.PublishCustomerBookingProjection;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.SeatOccupiedException;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;
import com.djnd.cinema_java_spring.web.rest.errors.UserAccessDeniedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingService {
    final BookingRepository bookingRepository;
    final ShowtimeRepository showtimeRepository;
    final PaymentHistoryService paymentHistoryService;
    final CustomerRepository customerRepository;
    final SeatService seatService;
    final ShowtimePriceService showtimePriceService;
    final TicketService ticketService;
    final CustomerVoucherRepository customerVoucherRepository;
    final VNPayService vnPayService;
    final StringRedisTemplate redisTemplate;
    final BookingDetailRepository bookingDetailRepository;
    final UserRepository userRepository;
    final PromotionRepository promotionRepository;
    final BookingDetailService bookingDetailService;
    // final TicketEventProducer ticketEventProducer;
    final BookingVoucherService bookingVoucherService;
    CustomerVoucherService customerVoucherService;
    final PaymentHistoryRepository paymentHistoryRepository;
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

    public ResBookingDTO createBookingByStaff(BookingRequestDTO request) {
        Long staffId = SecurityUtils.getCurrentUserIdOrNull();
        if (staffId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        User staff = userRepository.findWithDetailRoleById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        String roleName = staff.getRole().getName();
        List<String> allowedRoles = List.of(AuthoritiesConstants.ADMIN, AuthoritiesConstants.MANAGER,
                AuthoritiesConstants.STAFF);
        if (allowedRoles.stream().noneMatch(role -> role.equalsIgnoreCase(roleName))) {
            throw new UserAccessDeniedException("You do not have permission!");
        }

        // start holding seat and check exist seat at redis
        String showtimeRedisKey = this.getShowtimeKeyOrHoldingSeatAndCheckAtRedis(request.getShowtimeId(),
                request.getSeatIds());
        // end holding seat and check exist seat at redis

        // start check seat exist db, if exist remove seat in redis
        List<String> errorMessages = new ArrayList<>();
        List<Seat> seats = seatService.getSeatAvailable(request.getSeatIds(), errorMessages);

        if (!errorMessages.isEmpty()) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());
            throw new ResourceNotFoundException(String.join("\n", errorMessages));
        }
        // end check seat exist db, if exist remove seat in redis

        List<Integer> seatIdsAvaliables = seats.stream().map(Seat::getId).toList();
        // start check showtime booking must be before current
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found!"));
        LocalDateTime now = LocalDateTime.now();
        if (showtime.getStartDateTime().isBefore(now)) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());
            throw new RequestInvalidException("The movie had been shown!");

        }
        // end check showtime booking must be before current
        // start check seat with ticket sold
        ticketService.checkTicketWithSeatSold(request.getShowtimeId(), seatIdsAvaliables, errorMessages);
        if (!errorMessages.isEmpty()) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());

            throw new RequestInvalidException(String.join("\n", errorMessages));
        }
        // end check seat with ticket sold

        // start get price seat by start date time movie
        Map<SeatType, BigDecimal> priceSeatMap = showtimePriceService
                .getPriceSeatsByStartDateTime(showtime.getStartDateTime());
        // end get price seat by start date time movie

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<BookingDetail> bookingDetails = new ArrayList<>();
        String paymentMethod = request.getPaymentMethod();
        Booking booking = request.getIsNotMember()
                ? this.generateBookingForNotMember(paymentMethod)
                : this.generateBookingForMember(paymentMethod,
                        customerRepository.findById(request.getCustomerId())
                                .orElseThrow(() -> new ResourceNotFoundException("Customer not found!")));
        for (Seat seat : seats) {
            BigDecimal costSeat = priceSeatMap.get(seat.getType());
            if (costSeat == null) {
                errorMessages.add("Cannot get price config for seat: " + seat.getSeatRow() + seat.getSeatNo()
                        + " [Type: " + seat.getType() + "]");
            } else {
                // start generate booking detail
                BookingDetail detail = bookingDetailService.generateBookingDetail(booking, showtime, seat, costSeat);
                // end generate booking detail

                bookingDetails.add(detail);
                totalAmount = totalAmount.add(costSeat);

            }
        }

        if (!errorMessages.isEmpty()) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());
            throw new ResourceNotFoundException(String.join("\n", errorMessages));
        }
        booking.setTotalAmount(totalAmount);
        booking.setBookingDetails(bookingDetails);
        try {
            ResBookingDTO res = new ResBookingDTO();
            if (paymentMethod.equals(PaymentMethod.VNPAY.toString())) {
                BookingStatus pending = BookingStatus.PENDING;
                booking.setStatus(pending);
                booking = bookingRepository.save(booking);
                bookingRepository.flush(); // check unique
                paymentHistoryService.createHistoryWithStatus(booking, pending);
                String paymentUrl = vnPayService.createPaymentUrl(booking.getId(), totalAmount);

                res.setBookingId(booking.getId());
                res.setStatus(pending.toString());
                res.setPaymentUrl(paymentUrl);
            } else {
                BookingStatus success = BookingStatus.SUCCESS;
                booking.setStatus(success);
                booking = bookingRepository.save(booking);
                paymentHistoryService.createHistoryWithStatus(booking, success);
                ticketService.createTicketsWithBookingDetailsWhenPaymentBookingSuccess(booking, seatIdsAvaliables,
                        showtime.getId());
                bookingRepository.flush();

                this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, seatIdsAvaliables);
                res.setBookingId(booking.getId());
                res.setStatus(success.toString());

            }
            return res;

        } catch (Exception ex) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());

            if (ex instanceof DataIntegrityViolationException) {
                throw new RequestInvalidException(
                        "The system is busy because someone else has booked the same seat as you. Please try again!");
            }
            throw ex;

        }
    }

    public String getShowtimeKeyOrHoldingSeatAndCheckAtRedis(Long showtimeId, List<Integer> seatIds) {
        String showtimeRedisKey = "showtime:" + showtimeId + ":seats";
        List<String> argsRedis = new ArrayList<>();
        argsRedis.add(EXPIRE_TIME_HOLDING_SEATS);
        seatIds.forEach(seatId -> argsRedis.add(String.valueOf(seatId)));
        DefaultRedisScript<List<String>> redisScript = new DefaultRedisScript<>(LUA_HOLD_SEATS_AT_SHOWTIME);
        List<String> occupiedSeatIds = redisTemplate.execute(redisScript,
                Collections.singletonList(showtimeRedisKey),
                argsRedis.toArray());
        if (occupiedSeatIds != null && !occupiedSeatIds.isEmpty()) {
            List<Integer> errorOccupiedSeatIds = occupiedSeatIds.stream().map(Integer::parseInt).toList();
            throw new SeatOccupiedException("Some seat are already holding or sold!", errorOccupiedSeatIds);
        }
        return showtimeRedisKey;
    }

    // customer vnpay booking default
    public ResBookingDTO createBooking(BookingRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found customer!"));
        // start holding seat and check exist seat at redis
        String showtimeRedisKey = this.getShowtimeKeyOrHoldingSeatAndCheckAtRedis(request.getShowtimeId(),
                request.getSeatIds());
        // end holding seat and check exist seat at redis

        // start check seat exist db, if exist remove seat in redis
        List<String> errorMessages = new ArrayList<>();
        List<Seat> seats = seatService.getSeatAvailable(request.getSeatIds(), errorMessages);

        if (!errorMessages.isEmpty()) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());
            throw new ResourceNotFoundException(String.join("\n", errorMessages));
        }
        // end check seat exist db, if exist remove seat in redis

        List<Integer> seatIdsAvailable = seats.stream().map(Seat::getId).toList();
        // start check showtime booking must be before current
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found!"));
        LocalDateTime now = LocalDateTime.now();
        if (showtime.getStartDateTime().isBefore(now)) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());
            throw new RequestInvalidException("The movie had been shown!");

        }
        // end check showtime booking must be before current
        // start check seat with ticket sold
        ticketService.checkTicketWithSeatSold(request.getShowtimeId(), seatIdsAvailable, errorMessages);
        if (!errorMessages.isEmpty()) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());

            throw new RequestInvalidException(String.join("\n", errorMessages));
        }
        // end check seat with ticket sold

        // start get price seat by start date time movie
        Map<SeatType, BigDecimal> priceSeatMap = showtimePriceService
                .getPriceSeatsByStartDateTime(showtime.getStartDateTime());
        // end get price seat by start date time movie

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<BookingDetail> bookingDetails = new ArrayList<>();

        Booking booking = this.generateBookingForMember(PaymentMethod.VNPAY.toString(),
                customer);
        for (Seat seat : seats) {
            BigDecimal costSeat = priceSeatMap.get(seat.getType());
            if (costSeat == null) {
                errorMessages.add("Cannot get price config for seat: " + seat.getSeatRow() + seat.getSeatNo()
                        + " [Type: " + seat.getType() + "]");
            } else {
                // start generate booking detail
                BookingDetail detail = bookingDetailService.generateBookingDetail(booking, showtime, seat, costSeat);
                // end generate booking detail

                bookingDetails.add(detail);
                totalAmount = totalAmount.add(costSeat);

            }
        }

        if (!errorMessages.isEmpty()) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());
            throw new ResourceNotFoundException(String.join("\n", errorMessages));
        }
        BigDecimal finalTotalAmount = totalAmount;
        if (request.getVoucherId() != null) {
            Double discountPercent = customerVoucherService.getDiscountWithVoucherIdByCustomer(request.getVoucherId(),
                    userId);
            BigDecimal priceDiscount = totalAmount.multiply(new BigDecimal(String.valueOf(discountPercent)))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            finalTotalAmount = totalAmount.subtract(priceDiscount).max(BigDecimal.ZERO);
            bookingVoucherService.saveBookingVoucher(booking,
                    promotionRepository.getReferenceById(request.getVoucherId()), discountPercent, priceDiscount,
                    finalTotalAmount);
        }
        booking.setTotalAmount(finalTotalAmount);
        booking.setBookingDetails(bookingDetails);

        try {
            BookingStatus pending = BookingStatus.PENDING;
            booking.setStatus(pending);
            booking = bookingRepository.save(booking);
            paymentHistoryService.createHistoryWithStatus(booking, pending);
            if (request.getVoucherId() != null) {
                customerVoucherRepository.markVoucherAlreadyUsed(userId, request.getVoucherId());
            }
            // end save payment history
            bookingRepository.flush();
            String paymentUrl = vnPayService.createPaymentUrl(booking.getId(), finalTotalAmount);

            ResBookingDTO res = new ResBookingDTO();
            res.setBookingId(booking.getId());
            res.setPaymentUrl(paymentUrl);
            return res;

        } catch (Exception ex) {
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, request.getSeatIds());

            if (ex instanceof DataIntegrityViolationException) {
                throw new RequestInvalidException(
                        "The system is busy because someone else has booked the same seat as you. Please try again!");
            }
            throw ex;

        }

    }

    public Booking generateBookingForMember(String paymentMethod,
            Customer customer) {
        Booking newBooking = this.generateBooking(paymentMethod);
        newBooking.setCustomer(customer);
        return newBooking;
    }

    public Booking generateBookingForNotMember(String paymentMethod) {
        return this.generateBooking(paymentMethod);
    }

    public Booking generateBooking(String paymentMethod) {
        Booking newBooking = new Booking();
        newBooking.setBookingCode("BK-" + System.currentTimeMillis());
        newBooking.setPaymentMethod(paymentMethod);

        return newBooking;
    }

    @Transactional
    public Map<String, String> processVNPayCallback(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        Long bookingId = Long.parseLong(params.get("vnp_TxnRef"));
        String responseCode = params.get("vnp_ResponseCode");
        Booking booking = bookingRepository.findForUpdateDetailByIdWithVersion(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));
        Long showtimeId = booking.getBookingDetails().get(0).getShowtime().getId();
        String showtimeRedisKey = "showtime:" + showtimeId + ":seats";
        List<Integer> seatIds = booking.getBookingDetails().stream().map(detail -> detail.getSeat().getId())
                .toList();

        if (booking.getStatus() != BookingStatus.PENDING) {
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed");
            return response;
        }
        BigDecimal vnpTotalAmount = new BigDecimal(params.get("vnp_Amount")).divide(new BigDecimal(100));
        if (booking.getTotalAmount().compareTo(vnpTotalAmount) != 0) {
            response.put("RspCode", "04");
            response.put("Message", "Invalid Amount");
            return response;
        }
        if ("00".equals(responseCode)) {
            BookingStatus success = BookingStatus.SUCCESS;
            booking.setStatus(success);
            booking.setVersion(booking.getVersion() + 1);
            PaymentHistory history = new PaymentHistory();
            history.setBooking(booking);
            history.setStatus(success);
            paymentHistoryRepository.save(history);
            // init & save tickets
            ticketService.createTicketsWithBookingDetailsWhenPaymentBookingSuccess(booking, seatIds, showtimeId);

            // call producer
            // User customer = booking.getCustomer().getUser();
            // List<TicketDTO> tickets = ticketService.getTicketByBookingId(bookingId);
            // TicketMailEvent event = TicketMailEvent.builder()
            // .customerEmail(customer.getEmail())
            // .customerName(customer.getName())
            // .tickets(tickets)
            // .build();
            // ticketEventProducer.sendTicketMailEvent(event);

            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, seatIds);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;
        } else {
            BookingStatus failed = BookingStatus.FAILED;
            PaymentHistory history = new PaymentHistory();
            history.setBooking(booking);
            booking.setVersion(booking.getVersion() + 1);
            history.setStatus(failed);
            bookingDetailRepository.deleteAll(booking.getBookingDetails());
            booking.getBookingDetails().clear();
            paymentHistoryRepository.save(history);
            booking.setStatus(failed);
            this.removeSeatsWithShowtimeOnRedis(showtimeRedisKey, seatIds);

            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;
        }

    }

    public void removeSeatsWithShowtimeOnRedis(String redisKey, List<Integer> seatIds) {
        try {
            Object[] fields = seatIds.stream().map(String::valueOf).toArray();
            redisTemplate.opsForHash().delete(redisKey, fields);
        } catch (Exception ex) {
            log.error("Failed to rollback locked seats in Redis for key: {}", redisKey);
        }
    }

    @Transactional(readOnly = true)
    public UserDTO getCustomerByEmail(String email) {
        User user = userRepository.findOneByEmailAndActivatedIsTrue(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        if (user.getCustomer() == null) {
            throw new ResourceNotFoundException("User is not a customer: " + email);
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        return dto;
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllBookingWithPagination(Pageable pageable, String q) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        Page<Booking> page = bookingRepository.fetchAllWithPagination(q != null ? q : "", pageable);
        meta.setPage(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        List<PublishBookingDTO> publish = page.getContent().stream().map(booking -> {
            return PublishBookingDTO.builder()
                    .id(booking.getId())
                    .bookingCode(booking.getBookingCode())
                    .createdBy(booking.getCreatedBy())
                    .createdDate(booking.getCreatedDate())
                    .lastModifiedBy(booking.getLastModifiedBy())
                    .lastModifiedDate(booking.getLastModifiedDate())
                    .paymentMethod(booking.getPaymentMethod())
                    .status(booking.getStatus())
                    .totalAmount(booking.getTotalAmount()).build();
        }).toList();
        res.setResult(publish);
        return res;
    }

    @Transactional(readOnly = true)
    public PublishCustomerBookingProjection getPublishBookingDetail(Long bookingId) {
        return bookingRepository.getPublishCustomerBookingDetailById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));
    }
}
