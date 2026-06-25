package com.djnd.cinema_java_spring.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.projection.ProfileUserProjection;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    final CustomerRepository customerRepository;

    public void saveCustomerRegister(User user) {
        Customer customer = new Customer();
        customer.setUser(user);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public ProfileUserProjection getInformationAccount() {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        var currentUser = customerRepository.getInformationProfileUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        return currentUser;
    }

}
