package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.domain.entity.SeatMaintenance;
import com.djnd.cinema_java_spring.domain.entity.Ticket;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.SeatMaintenanceRepository;
import com.djnd.cinema_java_spring.repository.SeatRepository;
import com.djnd.cinema_java_spring.repository.TicketRepository;
import com.djnd.cinema_java_spring.service.dto.SeatMaintenanceDTO;
import com.djnd.cinema_java_spring.service.dto.SeatMaintenanceMailDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SeatMaintenanceService {
    final SeatRepository seatRepository;
    final SeatService seatService;
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
        String positionSeatMaintenance = seatService.createSeatMaintenance(seatMaintenanceDTO.getSeatId());
        var seatMaintenance =  this.createSeatMaintenance(seatMaintenanceDTO);
        List<Ticket> ticketsImpact = ticketRepository.getAllTicketCustomerAlreadyHasWithSeatMaintenanceAndTimeIn(seatMaintenance.getSeatId(), seatMaintenance.getStartTime(), seatMaintenance.getEndTime());
        List<SeatMaintenanceMailDTO> res  = new ArrayList<>();
        for(Ticket ticket : ticketsImpact) {
            SeatMaintenanceMailDTO seatMaintenanceMailDTO =  new SeatMaintenanceMailDTO();
            seatMaintenanceMailDTO.setStartDateTime(ticket.getShowtime().getStartDateTime());
            seatMaintenanceMailDTO.setMovieTitle(ticket.getShowtime().getMovie().getTitle());
            seatMaintenanceMailDTO.setPositionSeatMaintenance(positionSeatMaintenance);
            seatMaintenanceMailDTO.setEmailCustomerImpact(ticket.getBooking().getCustomer().getUser().getEmail());
            res.add(seatMaintenanceMailDTO);

        }
        notificationAsyncService.sendMailSeatMaintenanceForCustomer(res);
    }

}
