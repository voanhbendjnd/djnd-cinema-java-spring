package com.djnd.cinema_java_spring.service.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherCollectResultDTO {
    List<String> successTitles;
    List<String> errorMessages;
}
