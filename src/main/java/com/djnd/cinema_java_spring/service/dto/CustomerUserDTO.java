package com.djnd.cinema_java_spring.service.dto;

import java.io.Serial;
import java.io.Serializable;

import com.djnd.cinema_java_spring.config.Constants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Long id;
    @Size(min = 2, max = 100)
    String name;
    String gender;
    @Email
    String email;
    @Pattern(regexp = Constants.PHONE_REGEX, message = "Phone invalid format!")
    String phone;
    String identityCard;
    String address;
}
