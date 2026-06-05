package com.djnd.cinema_java_spring.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.domain.enumeration.UserGender;
import com.djnd.cinema_java_spring.service.UserService;
import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;
import com.djnd.cinema_java_spring.service.dto.RegisterUserDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.util.exception.RequestInvalidException;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserControllerV1 {
    UserService userService;

    @PostMapping
    @ApiMessage("Register user by admin")
    public ResponseEntity<AdminUserDTO> registerUser(@Valid @RequestBody RegisterUserDTO dto) {
        if (dto.getConfirmPassword().equals(dto.getConfirmPassword())) {
            throw new RequestInvalidException("Password not same thing!");
        }
        try {
            UserGender.valueOf(dto.getGender());
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(dto, dto.getPassword()));
        } catch (Exception ex) {
            throw new RequestInvalidException("Gender invalid format!");
        }
    }

}
