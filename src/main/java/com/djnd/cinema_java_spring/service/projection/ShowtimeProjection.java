package com.djnd.cinema_java_spring.service.projection;

import java.time.LocalDateTime;

public interface ShowtimeProjection {
    String getTitle();

    LocalDateTime getStartDateTime();

    LocalDateTime getEndDateTime();
}
