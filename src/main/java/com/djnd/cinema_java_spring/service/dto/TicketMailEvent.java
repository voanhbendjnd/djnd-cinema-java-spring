package com.djnd.cinema_java_spring.service.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TicketMailEvent {
    String customerEmail;
    String customerName;
    List<TicketDTO> tickets;

}
