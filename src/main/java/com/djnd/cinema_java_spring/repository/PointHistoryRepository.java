package com.djnd.cinema_java_spring.repository;

import com.djnd.cinema_java_spring.domain.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 2025-07-20: Repository quản lý PointHistory entity
// Chức năng: Lưu trữ lịch sử giao dịch điểm loyalty của khách hàng
@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    // 2025-07-20: Lấy lịch sử giao dịch điểm theo customerId, sắp xếp giảm dần theo thời gian
    List<PointHistory> findByCustomerIdOrderByCreatedDateDesc(Long customerId);
    
    // 2025-07-20: Lấy lịch sử giao dịch điểm theo customerId, sắp xếp tăng dần theo thời gian
    // Dùng để tính toán lại loyalty points từ lịch sử
    List<PointHistory> findByCustomerIdOrderByCreatedDateAsc(Long customerId);
}
