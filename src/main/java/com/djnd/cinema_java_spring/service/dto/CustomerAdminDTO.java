package com.djnd.cinema_java_spring.service.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerAdminDTO {
    Long id;
    String login;
    String name;
    String email;
    String phone;
    String gender;
    String address;
    String identityCard;
    Integer loyaltyPoints;
    Boolean activated;
    Instant createdDate;
    String createdBy;
    Instant lastModifiedDate;
    String lastModifiedBy;
}
