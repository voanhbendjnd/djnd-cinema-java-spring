package com.djnd.cinema_java_spring.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.domain.enumeration.UserGender;
import com.djnd.cinema_java_spring.service.MailService;
import com.djnd.cinema_java_spring.service.UserService;
import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.vm.ManagedUserVM;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/account")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AccountResource {
    final UserService userService;
    final MailService mailService;

    @PostMapping("/register")
    @ApiMessage("User register account")
    public ResponseEntity<AdminUserDTO> registerAccount(@Valid @RequestBody ManagedUserVM dto) {
        if (!dto.getConfirmPassword().equals(dto.getConfirmPassword())) {
            throw new RequestInvalidException("Password not same thing!");
        }
        try {
            UserGender.valueOf(dto.getGender());

        } catch (Exception ex) {
            throw new RequestInvalidException("Gender invalid format!");
        }
        var res = userService.registerUser(dto, dto.getPassword());
        mailService.sendActivationEmail(res);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

}
