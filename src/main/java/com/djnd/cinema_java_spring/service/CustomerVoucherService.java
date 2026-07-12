package com.djnd.cinema_java_spring.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.djnd.cinema_java_spring.service.dto.ResultPaginationVoucherCursor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.CustomerVoucher;
import com.djnd.cinema_java_spring.domain.entity.Promotion;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.CustomerVoucherRepository;
import com.djnd.cinema_java_spring.repository.PromotionRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.VoucherCollectResultDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;
import com.djnd.cinema_java_spring.web.rest.errors.UserAccessDeniedException;

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
    final PromotionService promotionService;

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

    @Transactional(readOnly = true)
    public ResultPaginationDTO getVoucherForCustomerClaim(String q, Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new UnauthorizedException("User not found!");
        }
        if (!customerRepository.existByCustomerId(userId)) {
            throw new UserAccessDeniedException("You do not have permission!");
        }
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        Page<Promotion> page = promotionRepository.fetchVoucherAvailableAndActiveWithPagination(pageable,
                q != null ? q.toLowerCase() : "", true,
                LocalDateTime.now(), userId);
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(promotionService::toDTO).toList());
        return res;
    }

    @Transactional(readOnly = true)
    public ResultPaginationVoucherCursor getVoucherAvailableByCustomerAlreadyClaim(int size, LocalDateTime cursor,
            Long voucherId) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new UnauthorizedException("You are not logged in!");
        }
        if (!customerRepository.existByCustomerId(userId)) {
            throw new UserAccessDeniedException("You do not have permission!");
        }

        Pageable pageable = PageRequest.of(0, size + 1);
        List<Promotion> voucherCustomerAvailable = customerVoucherRepository.getVoucherCustomerWithCursor(userId,
                voucherId, cursor,LocalDateTime.now(), pageable);
        boolean hasMore = voucherCustomerAvailable.size() > size;
        if (hasMore) {
            voucherCustomerAvailable.removeLast();
        }
        var res = new ResultPaginationVoucherCursor();
        Long nextVoucherId = null;
        LocalDateTime nextCursor = null;
        if (!voucherCustomerAvailable.isEmpty()) {
            nextVoucherId = voucherCustomerAvailable.getLast().getId();
            nextCursor = voucherCustomerAvailable.getLast().getStartTime();
        }
        res.setVoucherId(nextVoucherId);
        res.setNextCursor(nextCursor);
        res.setHasMore(hasMore);
        res.setResult(voucherCustomerAvailable.stream().map(promotionService::toDTO).toList());
        return res;
    }

    public Double getDiscountWithVoucherIdByCustomer(Long voucherId, Long customerId) {
        Promotion voucher = promotionRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found!"));
        CustomerVoucher customerVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(customerId, voucherId).orElseThrow(()-> new ResourceNotFoundException("Your voucher does not exist or you do not own it!"));
        LocalDateTime now = LocalDateTime.now();
        if(customerVoucher.isUsed()){
            throw new RequestInvalidException("This voucher has already been used!");        }
        if (!voucher.isActive() || voucher.getEndTime().isBefore(now)) {
                throw new RequestInvalidException("Voucher already expired!");
        }
        if(voucher.getStartTime().isAfter(now)) {
            throw new RequestInvalidException("Cannot use this voucher!");
        }
        if(voucher.getDiscountPercentage() >= 100 || voucher.getDiscountPercentage() <= 0) {
            throw new RequestInvalidException("Invalid voucher discount value!");
        }

        return voucher.getDiscountPercentage();
    }
}
