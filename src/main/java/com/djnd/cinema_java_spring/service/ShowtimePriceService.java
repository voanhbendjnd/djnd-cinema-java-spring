package com.djnd.cinema_java_spring.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.ShowtimePriceMatrix;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;
import com.djnd.cinema_java_spring.repository.ShowtimePriceRepository;
import com.djnd.cinema_java_spring.service.dto.ShowtimePriceDTO;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class ShowtimePriceService {
    final ShowtimePriceRepository showtimePriceRepository;

    public ShowtimePriceDTO createShowtimePrice(ShowtimePriceDTO dto) {
        SeatType seatType = SeatType.valueOf(dto.getSeatType());
        var showtimePrice = new ShowtimePriceMatrix();
        showtimePrice.setDayType(dto.getDayType());
        showtimePrice.setFinalPrice(dto.getFinalPrice());
        showtimePrice.setSeatType(seatType);
        showtimePrice.setStartTimeFrom(dto.getStartTimeFrom());
        showtimePrice.setStartTimeTo(dto.getStartTimeTo());
        var saveData = showtimePriceRepository.save(showtimePrice);
        return this.toRes(saveData);
    }

    public ShowtimePriceDTO updateShowtimePrice(ShowtimePriceDTO dto) {
        var showtimePrice = showtimePriceRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found!"));
        showtimePrice.setDayType(dto.getDayType());
        showtimePrice.setFinalPrice(dto.getFinalPrice());
        showtimePrice.setSeatType(SeatType.valueOf(dto.getSeatType()));
        showtimePrice.setStartTimeFrom(dto.getStartTimeFrom());
        showtimePrice.setStartTimeTo(dto.getStartTimeTo());
        return this.toRes(showtimePrice);
    }

    public List<ShowtimePriceDTO> getAllShowtimePrice() {
        return showtimePriceRepository.findAll().stream().map(this::toRes).toList();
    }

    public void deleteShowtimePrice(Integer id) {
        var entity = showtimePriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found!"));
        showtimePriceRepository.delete(entity);
    }

    public ShowtimePriceDTO toRes(ShowtimePriceMatrix entity) {
        var res = new ShowtimePriceDTO();
        res.setDayType(entity.getDayType());
        res.setFinalPrice(entity.getFinalPrice());
        res.setId(entity.getId());
        res.setSeatType(entity.getSeatType().toString());
        res.setStartTimeFrom(entity.getStartTimeFrom());
        res.setStartTimeTo(entity.getStartTimeTo());
        return res;
    }

    public Map<SeatType, BigDecimal> getPriceSeatsByStartDateTime(LocalDateTime startDateTime) {
        String dayType = (startDateTime.getDayOfWeek().getValue() >= 5) ? "WEEKEND" : "WEEKDAY";

        LocalTime showtimeAt = startDateTime.toLocalTime();
        List<ShowtimePriceMatrix> priceMatrixList = showtimePriceRepository.findByDayAndStartTime(dayType,
                showtimeAt);
        return priceMatrixList.stream()
                .collect(Collectors.toMap(ShowtimePriceMatrix::getSeatType, ShowtimePriceMatrix::getFinalPrice));
    }
}
