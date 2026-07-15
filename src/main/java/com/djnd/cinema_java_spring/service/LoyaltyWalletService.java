package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.PointHistory;
import com.djnd.cinema_java_spring.domain.enumeration.PointTransactionType;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.PointHistoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class LoyaltyWalletService {
    final CustomerRepository customerRepository;
    final PointHistoryRepository pointHistoryRepository;
    final CustomerService customerService;
    public void handleEarnPointCustomer(Customer customer, Integer baseAmount) {
        if(customer != null){
            Integer amountPoints = baseAmount / 100;
            customer.setLoyaltyPoints(customer.getLoyaltyPoints() + amountPoints);
            customerRepository.save(customer);
            PointHistory saveEarnPointHistory = PointHistory.builder()
                    .customerId(customer.getUserId())
                    .type(PointTransactionType.EARN)
                    .amountPoints(amountPoints)
                    .description("Customer payment successfully complete and receive reward points to wallet")
                    .build();
            pointHistoryRepository.save(saveEarnPointHistory);
            customerService.clearCacheCustomer(customer.getUserId());
        }
    }


}
