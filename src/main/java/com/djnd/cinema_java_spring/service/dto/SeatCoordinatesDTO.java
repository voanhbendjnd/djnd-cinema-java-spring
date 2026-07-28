package com.djnd.cinema_java_spring.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SeatCoordinatesDTO {
    private double x;
    private double y;
    private double z;
    // calculate seat coordinates
    public static SeatCoordinatesDTO calculate3DPosition(String seatCode, int totalColumns) {
        String rowLetter = seatCode.replaceAll("[^A-Za-z]", "");
        int colNum = Integer.parseInt(seatCode.replaceAll("[^0-9]", ""));
        int rowIndex = rowLetter.toUpperCase().charAt(0) - 'A';
        int colIndex = colNum - 1;
        double centerColumn = (totalColumns - 1) / 2.0;
        double x = (colIndex - centerColumn) * 0.8;
        double y = rowIndex * 0.3;
        double z = 5.0 + (rowIndex * 1.1);
        return new SeatCoordinatesDTO(x, y, z);
    }
}
