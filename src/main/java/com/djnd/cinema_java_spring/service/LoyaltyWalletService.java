package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.domain.entity.PointHistory;
import com.djnd.cinema_java_spring.domain.enumeration.PointTransactionType;
import com.djnd.cinema_java_spring.repository.CustomerRepository;
import com.djnd.cinema_java_spring.repository.PointHistoryRepository;
import com.djnd.cinema_java_spring.web.rest.errors.OperationCannotPerformedException;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.djnd.cinema_java_spring.service.dto.PointHistoryDTO;
import org.springframework.transaction.annotation.Transactional;

// 2025-07-20: Service quản lý ví điểm loyalty của khách hàng
// Chức năng: Cộng/trừ điểm loyalty, lưu lịch sử giao dịch điểm
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class LoyaltyWalletService {
    final CustomerRepository customerRepository;
    final PointHistoryRepository pointHistoryRepository;
    final CustomerService customerService;
    
    // 2025-07-20: Cộng điểm loyalty cho khách hàng sau khi thanh toán thành công
    // Tỷ lệ: 1 điểm / 100 đơn vị tiền
    @Transactional
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

    // 2025-07-20: Trừ điểm loyalty khi khách hàng dùng điểm đổi vé
    // Tỷ lệ: 1 điểm / 100 đơn vị tiền
    @Transactional
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

    // 2025-07-20: Lấy lịch sử giao dịch điểm của khách hàng
    public List<PointHistoryDTO> getPointHistory(Long userId) {
        return pointHistoryRepository.findByCustomerIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(ph -> {
                    PointHistoryDTO dto = new PointHistoryDTO();
                    dto.setId(ph.getId());
                    dto.setCustomerId(ph.getCustomerId());
                    dto.setAmountPoints(ph.getAmountPoints());
                    dto.setType(ph.getType());
                    dto.setDescription(ph.getDescription());
                    dto.setCreatedDate(ph.getCreatedDate());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 2025-07-20: Tính lại loyalty points từ lịch sử giao dịch
    // Logic: Tổng điểm EARN - Tổng điểm SPEND = Điểm hiện tại
    // Ví dụ: +100 -30 -40 = 30 điểm
    @Transactional
    public Integer recalculateLoyaltyPointsFromHistory(Long userId) {
        List<PointHistory> pointHistories = pointHistoryRepository.findByCustomerIdOrderByCreatedDateAsc(userId);
        
        Integer totalPoints = 0;
        for (PointHistory history : pointHistories) {
            if (history.getType() == PointTransactionType.EARN) {
                totalPoints += history.getAmountPoints();
            } else if (history.getType() == PointTransactionType.SPEND) {
                totalPoints -= history.getAmountPoints();
            }
        }
        
        // Cập nhật lại loyalty points cho customer
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found!"));
        customer.setLoyaltyPoints(totalPoints);
        customerRepository.save(customer);
        customerService.clearCacheCustomer(userId);
        
        return totalPoints;
    }

    // 2025-07-20: Tính loyalty points từ lịch sử (không lưu vào DB)
    // Chỉ dùng để kiểm tra hoặc hiển thị
    public Integer calculateLoyaltyPointsFromHistory(Long userId) {
        List<PointHistory> pointHistories = pointHistoryRepository.findByCustomerIdOrderByCreatedDateAsc(userId);
        
        Integer totalPoints = 0;
        for (PointHistory history : pointHistories) {
            if (history.getType() == PointTransactionType.EARN) {
                totalPoints += history.getAmountPoints();
            } else if (history.getType() == PointTransactionType.SPEND) {
                totalPoints -= history.getAmountPoints();
            }
        }
        
        return totalPoints;
    }

}
