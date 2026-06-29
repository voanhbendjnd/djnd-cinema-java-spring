package com.djnd.cinema_java_spring.web.rest.errors;

import java.io.Serial;
import java.util.List;

import lombok.Getter;

@Getter
public class SeatOccupiedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final List<Integer> occupiedSeatIds;

    public SeatOccupiedException(String message, List<Integer> occupiedSeatIds) {
        super(message);
        this.occupiedSeatIds = occupiedSeatIds;
    }

}
