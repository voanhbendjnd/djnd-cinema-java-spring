package com.djnd.cinema_java_spring.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.config.Constants;
import com.djnd.cinema_java_spring.domain.entity.Permission;
import com.djnd.cinema_java_spring.domain.entity.Role;
import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.domain.enumeration.LoginWith;
import com.djnd.cinema_java_spring.domain.enumeration.UserGender;
import com.djnd.cinema_java_spring.repository.RoleRepository;
import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.AdminUserDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.UserSecurityCacheDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;
import com.djnd.cinema_java_spring.web.rest.errors.UsernameAlreadyUsedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import tech.jhipster.security.RandomUtil;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class UserService {
    final UserRepository userRepository;
    final CacheManager cacheManager;
    final PasswordEncoder passwordEncoder;
    final RoleRepository roleRepository;

    public AdminUserDTO registerUser(AdminUserDTO userDTO, String password) {
        userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).ifPresent(existingUser -> {
            boolean removed = this.removeNoneActivatedUser(existingUser);
            if (!removed) {
                throw new UsernameAlreadyUsedException("Username already used!");
            }
        });
        if (userDTO.getEmail() != null) {
            userRepository.findOneByEmail(userDTO.getEmail().toLowerCase()).ifPresent(existingUser -> {
                boolean removed = this.removeNoneActivatedUser(existingUser);
                if (!removed) {
                    throw new UsernameAlreadyUsedException("Email already used!");
                }
            });
        }
        // init new user
        String encryptedPassword = passwordEncoder.encode(password);
        UserGender gender = UserGender.valueOf(userDTO.getGender());

        Role role = this.roleRepository.findOneByName(AuthoritiesConstants.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
        User newUser = User.builder()
                .login(userDTO.getLogin().toLowerCase())
                .password(encryptedPassword)
                .name(userDTO.getName())
                .email(userDTO.getEmail() != null ? userDTO.getEmail() : null)
                .gender(gender)
                .activationKey(RandomUtil.generateActivationKey())
                .phone(userDTO.getPhone())
                .loginWith(LoginWith.SYSTEM)
                .langKey(userDTO.getLangKey() != null ? userDTO.getLangKey() : Constants.DEFAULT_LANGUAGE)
                .role(role)
                .build();
        userRepository.save(newUser);
        this.clearUserCaches(newUser);
        return this.toAdminUserDTO(newUser);
    }

    public AdminUserDTO createUser(AdminUserDTO userDTO) {
        Role role = this.roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
        User user = User.builder()
                .login(userDTO.getLogin())
                .email(userDTO.getEmail() != null ? userDTO.getEmail() : null)
                .name(userDTO.getName())
                .gender(UserGender.valueOf(userDTO.getGender()))
                .password(this.passwordEncoder.encode(RandomUtil.generatePassword()))
                .resetKey(RandomUtil.generateResetKey())
                .resetDate(Instant.now())
                .langKey(userDTO.getLangKey() != null ? userDTO.getLangKey() : Constants.DEFAULT_LANGUAGE)
                .activated(true)
                .phone(userDTO.getPhone())
                .role(role)
                .loginWith(LoginWith.SYSTEM)
                .build();
        userRepository.save(user);
        this.clearUserCaches(user);
        return toAdminUserDTO(user);
    }

    public AdminUserDTO updateUser(AdminUserDTO userDTO) {

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        this.clearUserCaches(user);
        user.setLogin(userDTO.getLogin().toLowerCase());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setName(userDTO.getName());
        user.setPhone(userDTO.getPhone());
        user.setActivated(userDTO.isActivated());
        user.setLangKey(userDTO.getLangKey());
        user.setGender(UserGender.valueOf(userDTO.getGender()));
        userRepository.save(user);
        // concurrent
        this.clearUserCaches(user);

        return this.toAdminUserDTO(user);

    }

    public void updateUser(String name, String email, String langKey, String phone, String gender) {
        var userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        this.clearUserCaches(user);
        if (name != null)
            user.setName(name);
        if (email != null)
            user.setEmail(email);
        if (phone != null)
            user.setPhone(phone);
        if (gender != null)
            user.setGender(UserGender.valueOf(gender));
        user.setLangKey(langKey != null ? langKey : Constants.DEFAULT_LANGUAGE);
        userRepository.save(user);
        this.clearUserCaches(user);
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
        var cacheByLogin = cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE);
        if (cacheByLogin != null) {
            cacheByLogin.evictIfPresent(user.getLogin());
        }

        if (user.getEmail() != null) {
            var cacheByEmail = cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE);
            if (cacheByEmail != null) {
                cacheByEmail.evictIfPresent(user.getEmail());
            }
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

    @Cacheable(cacheNames = UserRepository.USERS_BY_LOGIN_CACHE, unless = "#result == null")
    public Optional<UserSecurityCacheDTO> getSecurityCacheByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login.toLowerCase()).map(this::getSecurityCache);
    }

    @Cacheable(cacheNames = UserRepository.USERS_BY_EMAIL_CACHE, unless = "#result == null")
    public Optional<UserSecurityCacheDTO> getSecurityCacheByEmail(String email) {
        return userRepository.findOneWithAuthoritiesByEmail(email.toLowerCase()).map(this::getSecurityCache);

    }

    public Optional<User> activateRegistration(String key) {
        return userRepository.findOneByActivationKey(key).map(user -> {
            user.setActivated(true);
            user.setActivationKey(null);
            this.clearUserCaches(user);
            return user;
        });
    }

    public Optional<AdminUserDTO> requestPasswordReset(String email) {
        return userRepository.findOneByEmailAndActivatedIsTrue(email.toLowerCase()).map(existingUser -> {
            existingUser.setResetKey(RandomUtil.generateResetKey());
            existingUser.setResetDate(Instant.now());
            this.clearUserCaches(existingUser);

            return this.toAdminUserDTO(existingUser);
        });

    }

    public Optional<User> completePasswordReset(String newPassword, String resetKey) {
        return userRepository.findOneByResetKey(resetKey)
                .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetKey(null);
                    this.clearUserCaches(user);
                    return user;
                });
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        var userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null)
            throw new UnauthorizedException("You are not logged in!");
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RequestInvalidException("Current password incorrect!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.clearUserCaches(user);

    }

    @Transactional
    public void deleteUser(String login) {
        var existingUser = userRepository.findOneByLogin(login.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        userRepository.delete(existingUser);
        this.clearUserCaches(existingUser);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllStaffCinemaWithPagination(Pageable pageable, String q) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = userRepository.fetchAllStaffUser(pageable, q != null ? q.toLowerCase() : "",
                List.of(AuthoritiesConstants.MANAGER, AuthoritiesConstants.STAFF));
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent());
        return res;
    }

    public UserSecurityCacheDTO getSecurityCache(User user) {
        var res = new UserSecurityCacheDTO();
        res.setEmail(user.getEmail());
        res.setLogin(user.getLogin());
        res.setActivated(user.isActivated());
        res.setGender(user.getGender());
        res.setPassword(user.getPassword());
        res.setLoginWith(user.getLoginWith());
        res.setLangKey(user.getLangKey());
        res.setName(user.getName());
        res.setId(user.getId());
        res.setRole(user.getRole().getName());
        var perms = user.getRole().getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
        perms.add(user.getRole().getName());
        res.setPermissions(perms);
        res.setSessionId(user.getSessionId());
        return res;
    }

    public void evictUserCache(String login, String email) {
        var cacheByLogin = cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE);
        if (cacheByLogin != null && login != null) {
            cacheByLogin.evictIfPresent(login.toLowerCase());
        }
        var cacheByEmail = cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE);
        if (cacheByEmail != null && email != null) {
            cacheByEmail.evictIfPresent(email.toLowerCase());
        }
    }

    public AdminUserDTO toAdminUserDTO(User user) {
        var userDTO = new AdminUserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setGender(user.getGender().toString());
        userDTO.setLogin(user.getLogin());
        userDTO.setName(user.getName());
        userDTO.setPhone(user.getPhone());
        userDTO.setLangKey(user.getLangKey());
        userDTO.setId(user.getId());
        userDTO.setCreatedBy(user.getCreatedBy());
        userDTO.setCreatedDate(user.getCreatedDate());
        userDTO.setLastModifiedBy(user.getLastModifiedBy());
        userDTO.setLastModifiedDate(user.getLastModifiedDate());
        userDTO.setLoginWith(user.getLoginWith());
        userDTO.setActivationKey(user.getActivationKey());
        userDTO.setResetKey(user.getResetKey());
        userDTO.setActivated(user.isActivated());
        return userDTO;

    }

}
