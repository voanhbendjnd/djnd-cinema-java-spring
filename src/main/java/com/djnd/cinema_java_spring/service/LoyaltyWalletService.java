package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.PointHistory;
import com.djnd.cinema_java_spring.domain.enumeration.PointTransactionType;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.PointHistoryRepository;
import com.djnd.cinema_java_spring.web.rest.errors.OperationCannotPerformedException;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class LoyaltyWalletService {
    final CustomerRepository customerRepository;
    final PointHistoryRepository pointHistoryRepository;
    final CustomerService customerService;
    public void handleEarnPointCustomer(Customer customer, Integer baseAmount) {
        if(customer != null){
            customer.setLoyaltyPoints(customer.getLoyaltyPoints() + baseAmount);
            customerRepository.save(customer);
            PointHistory saveEarnPointHistory = PointHistory.builder()
                    .customerId(customer.getUserId())
                    .type(PointTransactionType.EARN)
                    .amountPoints(baseAmount)
                    .description("Customer payment successfully complete and receive reward points to wallet")
                    .build();
            pointHistoryRepository.save(saveEarnPointHistory);
            customerService.clearCacheCustomer(customer.getUserId());
        }
    }

    public void handleSpendPointCustomer(Customer customer, Integer baseAmount) {
        if(customer != null){
            Integer currentPointsByCustomer = customer.getLoyaltyPoints();
            if(currentPointsByCustomer <= 0 || currentPointsByCustomer <  baseAmount){
                throw new OperationCannotPerformedException("Cannot use point exchange to ticket!");
            }
            customer.setLoyaltyPoints(currentPointsByCustomer - baseAmount);
            customerRepository.save(customer);
            PointHistory saveEarnPointHistory = PointHistory.builder()
                    .customerId(customer.getUserId())
                    .type(PointTransactionType.SPEND)
                    .amountPoints(baseAmount)
                    .description("Customer use point change with ticket system!")
                    .build();
            pointHistoryRepository.save(saveEarnPointHistory);
            customerService.clearCacheCustomer(customer.getUserId());
        }
    }


}
