package com.djnd.cinema_java_spring.service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.djnd.cinema_java_spring.domain.enumeration.SeatStatus;
import com.djnd.cinema_java_spring.repository.SeatMaintenanceRepository;
import com.djnd.cinema_java_spring.web.rest.errors.OperationCannotPerformedException;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.entity.Showtime;
import com.djnd.cinema_java_spring.domain.entity.ShowtimePriceMatrix;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;
import com.djnd.cinema_java_spring.repository.SeatRepository;
import com.djnd.cinema_java_spring.repository.ShowtimePriceRepository;
import com.djnd.cinema_java_spring.repository.ShowtimeRepository;
import com.djnd.cinema_java_spring.service.dto.CloneSeatLayoutDTO;
import com.djnd.cinema_java_spring.service.dto.ResSeatAtRoomBookingDTO;
import com.djnd.cinema_java_spring.service.projection.SeatLayoutDTO;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
@RequiredArgsConstructor
public class SeatService {
    final SeatRepository seatRepository;
    final ShowtimeRepository showtimeRepository;
    final ShowtimePriceRepository showtimePriceRepository;
    final SeatMaintenanceRepository seatMaintenanceRepository;
    public ResSeatAtRoomBookingDTO getSeatAtRoomLayoutByShowtime(Long showtimeId) {
        var res = new ResSeatAtRoomBookingDTO();
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found!"));
        String dayType = (showtime.getStartDateTime().getDayOfWeek().getValue() >= 5) ? "WEEKEND" : "WEEKDAY";
        LocalTime showtimeAt = showtime.getStartDateTime().toLocalTime();
        List<ShowtimePriceMatrix> priceMatrixList = showtimePriceRepository.findByDayAndStartTime(dayType, showtimeAt);
        Map<SeatType, BigDecimal> priceSeatMap = priceMatrixList.stream()
                .collect(Collectors.toMap(ShowtimePriceMatrix::getSeatType, ShowtimePriceMatrix::getFinalPrice));
        List<SeatLayoutDTO> seats = seatRepository.getSeatLayoutByShowtime(showtimeId);
        List<Integer> maintenanceSeatIds = seatMaintenanceRepository.findSeatIdsUnderMaintenance(showtime.getStartDateTime());

        List<CloneSeatLayoutDTO> resSeats = seats.stream().map(x -> {
            CloneSeatLayoutDTO seatDTO = new CloneSeatLayoutDTO();
            seatDTO.setId(x.id());
            seatDTO.setBookingStatus(x.bookingStatus());
            seatDTO.setSeatNo(x.seatNo());
            seatDTO.setSeatRow(x.seatRow());
//            seatDTO.setStatus(x.status());
            seatDTO.setType(x.type());
            return seatDTO;
        }).toList();
        for(CloneSeatLayoutDTO seat : resSeats){
            if(maintenanceSeatIds.contains(seat.getId())){
                seat.setStatus(SeatStatus.MAINTENANCE);
            }
            else{
                seat.setStatus(SeatStatus.ACTIVE);
            }
        }
        for (CloneSeatLayoutDTO seat : resSeats) {
            BigDecimal price = priceSeatMap.get(seat.getType());
            if (price != null) {
                seat.setPrice(price);
            } else {
                seat.setPrice(new BigDecimal(999999999));
            }
        }
        long totalSoldSeats = seats.stream().filter(seat -> seat.bookingStatus().equalsIgnoreCase("SOLD"))
                .mapToInt(SeatLayoutDTO::id).count();
        res.setSeats(resSeats);
        res.setTotalSeats(resSeats.size());
        res.setTotalSoldSeats(Math.toIntExact(totalSoldSeats));
        return res;
    }

    public List<Seat> getSeatAvailable(List<Integer> seatIds, List<String> errorMessages) {
        List<Seat> seats = seatRepository.findByIdIn(seatIds);
        List<Integer> seatIdsAvailable = seats.stream().map(Seat::getId).toList();
        for (Integer seatId : seatIds) {
            if (!seatIdsAvailable.contains(seatId)) {
                errorMessages.add("Seat with ID " + seatId + " not found!");
            }
        }
        return seats;
    }


    public String createSeatMaintenance(Integer seatId){
        Seat currentSeat = seatRepository.findById(seatId).orElseThrow(()-> new ResourceNotFoundException("Seat not found!"));
        if(currentSeat.getStatus() == SeatStatus.MAINTENANCE){
            throw new OperationCannotPerformedException("Seat is already maintenance!");
        }
        currentSeat.setStatus(SeatStatus.MAINTENANCE);
        seatRepository.save(currentSeat);
        return currentSeat.getSeatRow()  + currentSeat.getSeatNo();
    }



}
