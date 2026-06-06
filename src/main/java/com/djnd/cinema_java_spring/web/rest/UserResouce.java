package com.djnd.cinema_java_spring.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.domain.enumeration.UserGender;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class UserResouce {
    UserService userService;

    @PostMapping("/users")
    @ApiMessage("Register user by admin")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> registerUser(@Valid @RequestBody ManagedUserVM dto) {
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