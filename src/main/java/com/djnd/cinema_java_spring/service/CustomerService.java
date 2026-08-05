package com.djnd.cinema_java_spring.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.projection.AccountCustomerProjection;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

// 2025-07-20: Service quản lý Customer entity
// Chức năng: Lưu trữ thông tin khách hàng, quản lý cache thông tin tài khoản
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    final CustomerRepository customerRepository;
    final CacheManager cacheManager;

    // 2025-07-20: Lưu thông tin khách hàng khi đăng ký
    public void saveCustomerRegister(User user) {
        Customer customer = new Customer();
        customer.setUser(user);
        customerRepository.save(customer);
    }

    // 2025-07-20: Lấy thông tin tài khoản khách hàng hiện tại
    @Transactional(readOnly = true)
    public AccountCustomerProjection getInformationAccount() {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        var currentUser = customerRepository.getInformationProfileUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        return currentUser;
    }

    // 2025-07-20: Xóa cache thông tin khách hàng theo userId
    // Xóa cache thủ công qua CacheManager để đảm bảo loyalty points được cập nhật
    public void clearCacheCustomer(Long userId) {
        var cache = cacheManager.getCache(CustomerRepository.CACHE_INFORMATION_ACCOUNT_BY_USER_ID);
        if (cache != null) {
            cache.evictIfPresent(userId);
        }
    }

}
