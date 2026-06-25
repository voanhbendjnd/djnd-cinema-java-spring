package com.djnd.cinema_java_spring.web.rest;

import java.util.Map;

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

import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.CustomerService;
import com.djnd.cinema_java_spring.service.MailService;
import com.djnd.cinema_java_spring.service.UserService;
import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;
import com.djnd.cinema_java_spring.service.dto.PasswordChangeDTO;
import com.djnd.cinema_java_spring.service.projection.ProfileUserProjection;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.UsernameAlreadyUsedException;
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
    final UserRepository userRepository;
    final CustomerService customerService;

    private static class AccountResourceException extends ErrorResponseException {
        private AccountResourceException(String message) {
            super(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message), null);
        }
    }

    @GetMapping("/info")
    @ApiMessage("Get information account user already login")
    public ResponseEntity<ProfileUserProjection> getInformationAccount() {
        return ResponseEntity.ok(customerService.getInformationAccount());
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

    /**
     * 
     * @param name
     * @param email
     * @param langKey
     * @param gender
     * @return
     */
    @PostMapping
    @ApiMessage("Update info account")
    public ResponseEntity<Void> updateAccount(@Valid @RequestBody AdminUserDTO userDTO) {
        if (userDTO.getId() != null) {
            throw new RequestInvalidException("A new user cannot already have an ID");
        }
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new AccountResourceException("Current user login not found!");
        }
        // exist by email
        if (userDTO.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(userDTO.getEmail().toLowerCase(), userId))
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
        userService.updateUser(userDTO.getName(), userDTO.getEmail(), userDTO.getLangKey(), userDTO.getPhone(),
                userDTO.getGender());
        return ResponseEntity.ok(null);
    }

    @GetMapping("/activate")
    @ApiMessage("Activation account")
    public ResponseEntity<Void> activateAccount(@RequestParam(value = "key") String key) {
        var user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key!");
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/reset-password/finish")
    public ResponseEntity<Void> finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPasswordVM) {
        if (keyAndPasswordVM.getConfirmPassword().equals(keyAndPasswordVM.getNewPassword())) {
            if (isPasswordLengthInvalid(keyAndPasswordVM.getNewPassword())) {
                throw new RequestInvalidException("The password is not strong enough!");
            }
            var user = userService.completePasswordReset(keyAndPasswordVM.getNewPassword(), keyAndPasswordVM.getKey());
            if (!user.isPresent()) {
                throw new RequestInvalidException("Reset key invalid or not found!");
            }
            return ResponseEntity.ok(null);
        }
        throw new RequestInvalidException("Password not the same thing!");
    }

    @PostMapping("/reset-password/init")
    @ApiMessage("Required reset password from email")
    public ResponseEntity<Void> requiredResetPassword(@RequestBody Map<String, Object> request) {
        var email = (String) request.get("email");
        if (email != null) {
            var user = userService.requestPasswordReset(email);
            if (user.isPresent()) {
                mailService.sendPasswordResetMail(
                        user.orElseThrow(() -> new ResourceNotFoundException("User not found!")));
                return ResponseEntity.ok(null);
            }
        }

        throw new RequestInvalidException("User not found!");
    }

    @PostMapping("/change-password")
    @ApiMessage("Change password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        if (isPasswordLengthInvalid(passwordChangeDTO.getNewPassword())) {
            throw new RequestInvalidException("Password invalid!");
        }
        userService.changePassword(passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
        return ResponseEntity.ok(null);
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (StringUtils.isEmpty(password) ||
                password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
                password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH);
    }
}
