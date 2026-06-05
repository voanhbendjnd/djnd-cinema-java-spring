package com.djnd.cinema_java_spring.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.config.CustomUserDetails;
import com.djnd.cinema_java_spring.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service("userDetailsService")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class DomainUserDetailsService implements UserDetailsService {
    final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        username = username.toLowerCase();
        var user = userRepository.findOneByLoginOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Login or password incorrect!"));
        return new CustomUserDetails(user);
    }

}
