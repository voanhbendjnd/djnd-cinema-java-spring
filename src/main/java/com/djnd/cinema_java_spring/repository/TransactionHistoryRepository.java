package com.djnd.cinema_java_spring.repository;

import com.djnd.cinema_java_spring.domain.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    @Query(value = "select th.amount from TransactionHistory th where th.action = :action and th.ticketId = ticketId")
   Optional< BigDecimal> getAmountWithActionAndTicketId(@Param("action") String action, @Param("ticketId") Long ticketId);
}
