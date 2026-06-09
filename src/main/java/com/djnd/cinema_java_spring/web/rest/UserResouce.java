package com.djnd.cinema_java_spring.web.rest;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.config.Constants;
import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.MailService;
import com.djnd.cinema_java_spring.service.UserService;
import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.UsernameAlreadyUsedException;
import com.djnd.cinema_java_spring.web.rest.vm.ManagedUserVM;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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

    @PutMapping({ "/users", "/users/{login}" })
    @ApiMessage("Update user")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> updateUser(@Valid @RequestBody AdminUserDTO userDTO,
            @PathVariable(name = "login", required = false) @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        // exist by login
        if (userRepository.existsByLoginAndIdNot(userDTO.getLogin().toLowerCase(), userDTO.getId()))
            throw new UsernameAlreadyUsedException("Login already exist!");
        // exist by email
        if (userDTO.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(userDTO.getEmail().toLowerCase(), userDTO.getId()))
                throw new UsernameAlreadyUsedException("Email already exist!");
        }
        // exist by phone
        if (userDTO.getPhone() != null) {
            if (userRepository.existsByPhoneAndIdNot(userDTO.getPhone(), userDTO.getId())) {
                throw new RequestInvalidException("Phone already exist!");
            }
        }

        if (!ManagedUserVM.genderIsValid(userDTO.getGender())) {
            throw new RequestInvalidException("Gender invalid format!");
        }
        return ResponseEntity.ok(userService.updateUser(userDTO));

    }

    @GetMapping("/users")
    @ApiMessage("Get all user with pagination")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ResultPaginationDTO> fetchAllPublishUser(@RequestParam(name = "q", required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUserWithPagination(pageable, q));
    }

    @DeleteMapping("/users/{login}")
    @ApiMessage("Delete user by login")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")") // get from security context
    public ResponseEntity<Void> deleteUserByLogin(@PathVariable(name = "login") String login) {
        userService.deleteUser(login);
        return ResponseEntity.ok(null);
    }
}