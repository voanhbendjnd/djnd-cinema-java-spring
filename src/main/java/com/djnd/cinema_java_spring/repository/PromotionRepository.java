package com.djnd.cinema_java_spring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE (:q IS NULL OR :q = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Promotion> fetchAllWithPagination(Pageable pageable, @Param("q") String q);


    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    Long countByIdIn(List<Long> promotionIds);

    List<Promotion> findByIdIn(List<Long> promotionIds);

    @Query(value = """
            SELECT p FROM Promotion p
            WHERE lower(p.title) like concat('%', :q, '%') and p.releaseDate <= :current and p.isActive = :active
            and not exists(
                select cv from CustomerVoucher cv
                where
                cv.voucher = p and
                cv.customer.userId = :customerId
            )
            """, countQuery = """
            SELECT count(p) FROM Promotion p
                   WHERE lower(p.title) like concat('%', :q, '%') and p.releaseDate <= :current and p.isActive = :active
                   and not exists(
                       select cv from CustomerVoucher cv
                       where
                        cv.voucher = p and
                       cv.customer.userId = :customerId
                   )
            """

    )
    Page<Promotion> fetchVoucherAvailableAndActiveWithPagination(Pageable pageable, @Param("q") String q,
            @Param("active") boolean isActive, @Param("current") LocalDateTime current,
            @Param("customerId") Long customerId);
}
