package com.djnd.cinema_java_spring.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.MailService;
import com.djnd.cinema_java_spring.service.UserService;
import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.UsernameAlreadyUsedException;
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
    final UserService userService;
    final UserRepository userRepository;
    final MailService mailService;

    @PostMapping("/users")
    @ApiMessage("Create user by admin")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> registerUser(@Valid @RequestBody AdminUserDTO dto) {
        if (dto.getId() != null) {
            throw new RequestInvalidException("A new user cannot already have an ID");
        } else if (userRepository.findOneByLogin(dto.getLogin().toLowerCase()).isPresent()) {
            throw new UsernameAlreadyUsedException("Login already exsit!");
        } else if (userRepository.findOneByEmail(dto.getEmail().toLowerCase()).isPresent()) {
            throw new UsernameAlreadyUsedException("Login already exsit!");
        }
        if (dto.getPhone() != null) {
            if (userRepository.userExistByPhone(dto.getPhone()))
                throw new RequestInvalidException("Phone already exist!");
        }
        if (!ManagedUserVM.genderIsValid(dto.getGender())) {
            throw new RequestInvalidException("Gender invalid!");
        }

        var user = userService.createUser(dto);
        mailService.sendCreationEmail(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}