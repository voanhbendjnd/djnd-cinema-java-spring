package com.djnd.cinema_java_spring.repository;

import com.djnd.cinema_java_spring.domain.entity.SeatMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatMaintenanceRepository extends JpaRepository<SeatMaintenance, Integer> {
}
