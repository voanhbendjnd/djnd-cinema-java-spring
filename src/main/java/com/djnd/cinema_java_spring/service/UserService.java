package com.djnd.cinema_java_spring.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Role;
import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.domain.enumeration.LoginWith;
import com.djnd.cinema_java_spring.domain.enumeration.UserGender;
import com.djnd.cinema_java_spring.repository.RoleRepository;
import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;
import com.djnd.cinema_java_spring.util.exception.ResourceNotFoundException;
import com.djnd.cinema_java_spring.util.exception.UsernameAlreadyUsedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import tech.jhipster.security.RandomUtil;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class UserService {
    UserRepository userRepository;
    CacheManager cacheManager;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    public AdminUserDTO registerUser(AdminUserDTO userDTO, String password) {
        userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).ifPresent(existingUser -> {
            boolean removed = this.removeNoneActivatedUser(existingUser);
            if (!removed) {
                throw new UsernameAlreadyUsedException("Login name already used!");
            }
        });
        if (userDTO.getEmail() != null) {
            userRepository.findOneByEmail(userDTO.getEmail().toLowerCase()).ifPresent(existingUser -> {
                boolean removed = this.removeNoneActivatedUser(existingUser);
                if (!removed) {
                    throw new UsernameAlreadyUsedException("Login name already used");
                }
            });
        }
        // init new user
        String encryptedPassword = passwordEncoder.encode(password);
        UserGender gender = UserGender.valueOf(userDTO.getGender());
        Role role = this.roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
        // Role role =
        // roleRepository.findOneByNameIgnoreCase(AuthoritiesConstants.CUSTOMER)
        // .orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
        User newUser = User.builder()
                .login(userDTO.getLogin().toLowerCase())
                .password(encryptedPassword)
                .name(userDTO.getName())
                .email(userDTO.getEmail() != null ? userDTO.getEmail() : null)
                .gender(gender)
                .activationKey(RandomUtil.generateActivationKey())
                .phone(userDTO.getPhone())
                .loginWith(LoginWith.SYSTEM)
                .role(role)
                .build();
        userRepository.save(newUser);
        this.clearUserCaches(newUser);
        return this.toAdminUserDTO(newUser);
    }

    public AdminUserDTO toAdminUserDTO(User user) {
        var userDTO = new AdminUserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setGender(user.getGender().toString());
        userDTO.setLogin(user.getLogin());
        userDTO.setName(user.getName());
        userDTO.setPhone(user.getPhone());
        userDTO.setId(user.getId());
        userDTO.setCreatedBy(user.getCreatedBy());
        userDTO.setCreatedDate(user.getCreatedDate());
        userDTO.setLastModifiedBy(user.getLastModifiedBy());
        userDTO.setLastModifiedDate(user.getLastModifiedDate());
        userDTO.setLoginWith(user.getLoginWith());
        return userDTO;

    }

    /**
     * check user when after 3 days not logged in remove account
     * 0s 0mi 1am *everyday *everyweek
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActiveUsers() {
        this.userRepository
                .findAllByActivatedIsFalseAndActivationKeyNotNullAndCreatedDateBefore(
                        Instant.now().minus(3, ChronoUnit.DAYS))
                .forEach(user -> {
                    this.userRepository.delete(user);
                    this.clearUserCaches(user);
                });
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE))
                .evictIfPresent(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE))
                    .evictIfPresent(user.getEmail());
        }
    }

    /**
     * remove user if user non activated
     * and skip transaction with flush (remove user moment)
     * clear old cache
     */
    private boolean removeNoneActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        this.userRepository.delete(existingUser);
        this.userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }
}
