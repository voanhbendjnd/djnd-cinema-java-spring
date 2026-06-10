package com.djnd.cinema_java_spring.service;

import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RoleService {
    final RoleRepository roleRepository;
}
