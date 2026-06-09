package com.djnd.cinema_java_spring.service.dto;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordChangeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "Current password null!")
    String currentPassword;
    @NotBlank(message = "New password null!")
    String newPassword;
}
