package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.Ticket;
import com.djnd.cinema_java_spring.domain.entity.TransactionHistory;
import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.domain.enumeration.TransactionAction;
import com.djnd.cinema_java_spring.repository.BookingRepository;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.TicketRepository;
import com.djnd.cinema_java_spring.repository.TransactionHistoryRepository;
import com.djnd.cinema_java_spring.service.dto.TransactionHistoryDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class TransactionHistoryService {
    final TransactionHistoryRepository transactionHistoryRepository;
    final BookingRepository bookingRepository;
    final TicketRepository ticketRepository;
    final TicketService ticketService;
    public void createTransactionHistoryWithBookingTicketSuccess(Map<Long, BigDecimal> ticketPriceMap){
        this.init(ticketPriceMap, TransactionAction.IN.toString(), "Customer pay for ticket success");
    }

    public void refundForCustomer(TransactionHistoryDTO transactionHistoryDTO) {
        BigDecimal amountRefund= transactionHistoryRepository.getAmountWithActionAndTicketId(TransactionAction.IN.toString(),transactionHistoryDTO.getTicketId()).orElseThrow(()-> new ResourceNotFoundException("Amount with ticket not found!"));
        Ticket currentTicket = ticketRepository.findWithDetailBookingById(transactionHistoryDTO.getTicketId()).orElseThrow(()-> new ResourceNotFoundException("Ticket not found!"));
        if(LocalDateTime.now().isBefore(currentTicket.getShowtime().getStartDateTime())){
            throw new RequestInvalidException("Showtime already showing cannot operation!");
        }
        this.processDeleteTicketAndChangeStatusBooking(currentTicket);
            Map<Long, BigDecimal> ticketPriceMap = new HashMap<>();
            ticketPriceMap.put(transactionHistoryDTO.getTicketId(), amountRefund);
            this.init(ticketPriceMap ,TransactionAction.OUT.toString(), transactionHistoryDTO.getReason());
    }
    private void processDeleteTicketAndChangeStatusBooking(Ticket ticket){
        ticketService.deleteOneTicket(ticket.getId());
        Booking currentBooking = ticket.getBooking();
        currentBooking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(currentBooking);
    }
    public void init(Map<Long, BigDecimal> ticketPriceMap, String action, String reason){
        List<TransactionHistory> transactionHistories = new ArrayList<>();
        ticketPriceMap.forEach((ticketId, ticketPrice) -> {
            TransactionHistory transactionHistory = new TransactionHistory();
            transactionHistory.setAmount(ticketPrice);
            transactionHistory.setAction(action);
            transactionHistory.setReason(reason);
            transactionHistory.setTicketId(ticketId);
            transactionHistories.add(transactionHistory);
        });

        transactionHistoryRepository.saveAll(transactionHistories);
    }


}
