package com.djnd.cinema_java_spring.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VNPayService {
    public String createPaymentUrl(Long bookingId, BigDecimal totalAmount) {
        return "1";
    }
}
