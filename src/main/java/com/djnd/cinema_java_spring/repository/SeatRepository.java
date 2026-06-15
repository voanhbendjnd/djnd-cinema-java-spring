package com.djnd.cinema_java_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

}
