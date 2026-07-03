package com.djnd.cinema_java_spring.repository;

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

    Optional<Promotion> findById(Long id);

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);
}
