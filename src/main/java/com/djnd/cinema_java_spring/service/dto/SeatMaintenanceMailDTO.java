package com.djnd.cinema_java_spring.service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatMaintenanceMailDTO {
    String emailCustomerImpact;
    String movieTitle;
    LocalDateTime startDateTime;
    String positionSeatMaintenance;
}
