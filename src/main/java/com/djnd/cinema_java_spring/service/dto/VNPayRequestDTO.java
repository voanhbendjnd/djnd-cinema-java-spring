package com.djnd.cinema_java_spring.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class VNPayRequestDTO {
    @JsonProperty("vnp_TxnRef")
    String vnpTxnRef;
    @JsonProperty("vnp_ResponseCode")
    String responseCode;

}
