package com.djnd.cinema_java_spring.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.domain.enumeration.UserGender;
import com.djnd.cinema_java_spring.service.MailService;
import com.djnd.cinema_java_spring.service.UserService;
import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.vm.KeyAndPasswordVM;
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

    private static class AccountResourceException extends ErrorResponseException {
        private AccountResourceException(String message) {
            super(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message), null);
        }
    }

    @PostMapping("/register")
    @ApiMessage("User register account")
    public ResponseEntity<AdminUserDTO> registerAccount(@Valid @RequestBody ManagedUserVM dto) {
        if (!dto.getConfirmPassword().equals(dto.getConfirmPassword())) {
            throw new RequestInvalidException("Password not same thing!");
        }
        if (!ManagedUserVM.genderIsValid(dto.getGender())) {
            throw new RequestInvalidException("Gender invalid!");
        }
        var res = userService.registerUser(dto, dto.getPassword());
        mailService.sendActivationEmail(res);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/activate")
    @ApiMessage("Activation account")
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        var user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key!");
        }
        return ResponseEntity.ok("Account activation successful");
    }

    @PostMapping("/reset-password/finish")
    public ResponseEntity<String> finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPasswordVM) {
        if (keyAndPasswordVM.getConfirmPassword().equals(keyAndPasswordVM.getNewPassword())) {
            if (isPasswordLengthInvalid(keyAndPasswordVM.getNewPassword())) {
                throw new RequestInvalidException("The password is not strong enough!");
            }
            var user = userService.completePasswordReset(keyAndPasswordVM.getNewPassword(), keyAndPasswordVM.getKey());
            if (!user.isPresent()) {
                throw new RequestInvalidException("Reset key invalid or over date!");
            }
            return ResponseEntity.ok("Reset password successful");
        }
        throw new RequestInvalidException("Password not the same thing!");
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (StringUtils.isEmpty(password) ||
                password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
                password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH);
    }

}
