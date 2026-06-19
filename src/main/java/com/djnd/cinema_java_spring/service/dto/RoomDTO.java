package com.djnd.cinema_java_spring.service.dto;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Integer id;
    @NotBlank(message = "Room name not found!")
    String name;
    @NotBlank(message = "Room status not found!")
    String status;
    Integer totalRows;
    Integer totalCols;
    Integer totalSeats;
    @NotBlank(message = "Room type not found!")
    String type;
}
