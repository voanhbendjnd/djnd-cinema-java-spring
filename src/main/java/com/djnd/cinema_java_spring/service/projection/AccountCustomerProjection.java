package com.djnd.cinema_java_spring.service.projection;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import com.djnd.cinema_java_spring.domain.enumeration.UserGender;

public record AccountCustomerProjection(Long id, String name, String login, String email, UserGender gender,
        String phone, String avatarUrl, boolean activated, Instant createdDate, String createdBy,
        Instant InstantLastModifiedDate,
        String lastModifiedBy, String identityCard, String address,
        Integer loyaltyPoints, Instant dateOfBirth) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
