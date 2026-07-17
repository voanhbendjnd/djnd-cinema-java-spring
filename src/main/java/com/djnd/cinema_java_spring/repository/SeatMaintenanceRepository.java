package com.djnd.cinema_java_spring.repository;

import com.djnd.cinema_java_spring.domain.entity.SeatMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatMaintenanceRepository extends JpaRepository<SeatMaintenance, Integer> {

    @Query(value = "select sm.seatId from SeatMaintenance sm where :showtimeStartTime between sm.startTime and sm.endTime")
    List<Integer> findSeatIdsUnderMaintenance(@Param("showtimeStartTime") LocalDateTime showtimeStartTime);
}
