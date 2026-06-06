package com.djnd.cinema_java_spring.security;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.service.UserService;
import com.djnd.cinema_java_spring.service.dto.UserSecurityCacheDTO;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service("userDetailsService")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class DomainUserDetailsService implements UserDetailsService {
    final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String lowercaseUsername = username.toLowerCase();
        if (new EmailValidator().isValid(lowercaseUsername, null)) {
            return userService.getSecurityCacheByEmail(lowercaseUsername)
                    .map(user -> createSpringSecurityUser(lowercaseUsername, user))
                    .orElseThrow(() -> new UsernameNotFoundException("Login or password incorrect!"));
        }
        return userService.getSecurityCacheByLogin(lowercaseUsername)
                .map(user -> createSpringSecurityUser(lowercaseUsername, user))
                .orElseThrow(() -> new UsernameNotFoundException("Login or password incorrect!"));
    }

    private UserDetails createSpringSecurityUser(String username,
            UserSecurityCacheDTO user) {
        if (!user.isActivated()) {
            throw new UserNotActivatedException("User " + username + " was not activated");
        }
        return new CustomUserDetails(user);
    }

}
