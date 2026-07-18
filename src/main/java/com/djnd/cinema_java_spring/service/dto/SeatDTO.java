package com.djnd.cinema_java_spring.service.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatDTO {
    Integer id;
    String seatRow;
    Integer seatNo;
    String type;
    String status;
    List<SeatMaintenanceDTO> seatMaintenances;
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SeatMaintenanceDTO{
        String reason;
        LocalDateTime startTime;
        LocalDateTime endTime;
    }
}
