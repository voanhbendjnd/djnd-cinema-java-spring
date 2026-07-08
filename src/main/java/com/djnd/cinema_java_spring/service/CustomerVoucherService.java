package com.djnd.cinema_java_spring.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.CustomerVoucher;
import com.djnd.cinema_java_spring.domain.entity.Promotion;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.CustomerVoucherRepository;
import com.djnd.cinema_java_spring.repository.PromotionRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.VoucherCollectResultDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CustomerVoucherService {
    final CustomerVoucherRepository customerVoucherRepository;
    final CustomerRepository customerRepository;
    final PromotionRepository promotionRepository;

    @Transactional
    public VoucherCollectResultDTO collectVouchersByCustomer(List<Long> voucherIds) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found!"));
        List<Promotion> existingPromotions = promotionRepository.findByIdIn(voucherIds);
        if (existingPromotions.size() != voucherIds.size()) {
            throw new RequestInvalidException("Some vouchers were not found!");
        }
        List<String> errorMessages = new ArrayList<>();
        List<String> successTitles = new ArrayList<>();

        List<String> existingTitleVouchers = customerVoucherRepository.getTitleVoucherByCustomerClaim(userId,
                voucherIds);
        if (!existingTitleVouchers.isEmpty()) {
            for (String title : existingTitleVouchers) {
                errorMessages.add(title + " you already claim!");
            }
            throw new RequestInvalidException(String.join("\n", errorMessages));
        }
        LocalDateTime now = LocalDateTime.now();
        List<CustomerVoucher> newCustomerVouchers = new ArrayList<>();
        for (Promotion voucher : existingPromotions) {
            if (now.isAfter(voucher.getEndTime())) {
                errorMessages.add(String.format("Voucher %s is expired!", voucher.getTitle()));

            }
            if (now.isBefore(voucher.getReleaseDate())) {
                errorMessages.add(String.format("Voucher %s is before now!", voucher.getTitle()));

            }
            if (!voucher.isActive()) {
                errorMessages.add(String.format("Voucher %s inactive", voucher.getTitle()));
            }
            if (voucher.getQuantity() < 1) {
                errorMessages.add(String.format("Voucher %s sold out!", voucher.getTitle()));
            } else {
                voucher.setQuantity(voucher.getQuantity() - 1);
                CustomerVoucher customerVoucher = new CustomerVoucher();
                customerVoucher.setCustomer(customer);
                customerVoucher.setVoucher(voucher);
                newCustomerVouchers.add(customerVoucher);
                successTitles.add(voucher.getTitle());

            }

        }
        if (!newCustomerVouchers.isEmpty()) {
            try {
                customerVoucherRepository.saveAll(newCustomerVouchers);
                promotionRepository.saveAll(existingPromotions);

            } catch (DataIntegrityViolationException ex) {
                throw new RequestInvalidException("System is busy, please try again!");
            }
        }
        VoucherCollectResultDTO res = new VoucherCollectResultDTO();
        res.setErrorMessages(errorMessages);
        res.setSuccessTitles(successTitles);
        return res;
    }
}
