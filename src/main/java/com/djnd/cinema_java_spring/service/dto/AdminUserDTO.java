package com.djnd.cinema_java_spring.service.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import com.djnd.cinema_java_spring.config.constants.Constants;
import com.djnd.cinema_java_spring.domain.enumeration.LoginWith;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Long id;
    @NotBlank(message = "Login null")
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    String login;
    @Size(min = 2, max = 100)
    String name;
    String gender;
    @Email
    String email;
    @Pattern(regexp = Constants.PHONE_REGEX)
    String phone;
    @NotNull(message = "Role null")
    Integer roleId;
    boolean activated = false;
    Instant createdDate, lastModifiedDate;
    String createdBy, lastModifiedBy;
    LoginWith loginWith;
}
