package com.djnd.cinema_java_spring.web.rest.vm;

import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class ManagedUserVM extends AdminUserDTO {
    @NotBlank(message = "Passowrd null!")
    @Size(min = 4, max = 100, message = "Lenght password min 4 characters max 100 characters")
    String password;
    @NotBlank(message = "Confirm passowrd null!")
    @Size(min = 4, max = 100, message = "Lenght confirm password min 4 characters max 100 characters")
    String confirmPassword;
}
