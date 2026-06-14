package com.djnd.cinema_java_spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    @Query(value = "select m from Movie m where lower(m.title) like lower(concat('%', :q, '%'))", countQuery = "select count(m) from Movie m where lower(m.title) like lower(concat('%', :q, '%'))")
    Page<Movie> fetchAllWithPagination(@Param("q") String q, Pageable pageable);
}
