package com.djnd.cinema_java_spring.web.rest.vm;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
@Setter
public class LoginVM {
    @NotBlank(message = "Login null")
    String login;
    @NotBlank(message = "Password null")
    String password;
}
