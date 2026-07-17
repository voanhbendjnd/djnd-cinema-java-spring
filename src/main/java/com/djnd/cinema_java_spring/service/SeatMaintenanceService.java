package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.entity.SeatMaintenance;
import com.djnd.cinema_java_spring.domain.entity.Ticket;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.SeatMaintenanceRepository;
import com.djnd.cinema_java_spring.repository.SeatRepository;
import com.djnd.cinema_java_spring.repository.TicketRepository;
import com.djnd.cinema_java_spring.service.dto.SeatMaintenanceDTO;
import com.djnd.cinema_java_spring.service.dto.SeatMaintenanceMailDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SeatMaintenanceService {
    final LoyaltyWalletService loyaltyWalletService;
    final SeatRepository seatRepository;
    final CustomerRepository customerRepository;
    final SeatMaintenanceRepository seatMaintenanceRepository;
    final NotificationAsyncService notificationAsyncService;
    final TicketRepository ticketRepository;
    public SeatMaintenance createSeatMaintenance(SeatMaintenanceDTO seatMaintenanceDTO) {
        if(!seatRepository.existById(seatMaintenanceDTO.getSeatId())) {
            throw new RequestInvalidException("Seat does not exist");
        }
       SeatMaintenance seatMaintenance = SeatMaintenance.builder()
               .seatId(seatMaintenanceDTO.getSeatId())
               .startTime(seatMaintenanceDTO.getStartTime())
               .endTime(seatMaintenanceDTO.getEndTime())
               .reason(seatMaintenanceDTO.getReason())
               .build();
     return seatMaintenanceRepository.save(seatMaintenance);

    }
    @Transactional
    public void handleSeatWhenNeedMaintenance(SeatMaintenanceDTO seatMaintenanceDTO) {
        Seat currentSeat = seatRepository.findById(seatMaintenanceDTO.getSeatId()).orElseThrow(()-> new ResourceNotFoundException("Seat not found!"));

        String positionSeatMaintenance = currentSeat.getSeatRow() + currentSeat.getSeatNo();
        var seatMaintenance =  this.createSeatMaintenance(seatMaintenanceDTO);
        List<Ticket> ticketsImpact = ticketRepository.getAllTicketCustomerAlreadyHasWithSeatMaintenanceAndTimeIn(seatMaintenance.getSeatId(), seatMaintenance.getStartTime(), seatMaintenance.getEndTime());
        List<SeatMaintenanceMailDTO> mailList  = new ArrayList<>();
        for(Ticket ticket : ticketsImpact) {
            Customer customer = ticket.getBooking().getCustomer();
            SeatMaintenanceMailDTO seatMaintenanceMailDTO =  new SeatMaintenanceMailDTO();
            seatMaintenanceMailDTO.setStartDateTime(ticket.getShowtime().getStartDateTime());
            seatMaintenanceMailDTO.setMovieTitle(ticket.getShowtime().getMovie().getTitle());
            seatMaintenanceMailDTO.setPositionSeatMaintenance(positionSeatMaintenance);
            seatMaintenanceMailDTO.setEmailCustomerImpact(customer.getUser().getEmail());
            loyaltyWalletService.handleEarnPointCustomer(customer, ticket.getPrice().intValue());
            mailList.add(seatMaintenanceMailDTO);

        }
        if(!mailList.isEmpty()){
            notificationAsyncService.sendMailSeatMaintenanceForCustomer(mailList);

        }
    }



}
