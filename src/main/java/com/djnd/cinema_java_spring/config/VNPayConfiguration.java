package com.djnd.cinema_java_spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class VNPayConfiguration {
    @Value("${vnp.payUrl}")
    private String payUrl;

    @Value("${vnp.tmnCode}")
    private String tmnCode;

    @Value("${vnp.hashSecret}")
    private String hashSecret;

    @Value("${vnp.apiUrl}")
    private String apiUrl;

    @Value("${vnp.returnUrl}")
    private String returnUrl;
}
