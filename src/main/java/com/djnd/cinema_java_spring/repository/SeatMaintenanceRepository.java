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

    @Query(value = """
select exists(
select 1 from SeatMaintenance sm where sm.seatId = :seatId
and (
(sm.startTime between :newStartTime and :newEndTime)
or (sm.endTime between :newStartTime and :newEndTime)
or (:newStartTime between sm.startTime and sm.endTime)
)
)
    """)
    boolean existSeatMaintenanceBySeatIdAndTimeIn(@Param("seatId") Integer seatId, @Param("newStartTime") LocalDateTime newStartTime, @Param("newEndTime") LocalDateTime newEndTime);
    @Query(value = "select sm from SeatMaintenance sm join Seat s on s.id = sm.seatId join Room r on r.id = s.room.id where r.id = :roomId")
//    @Query(value = "select sm from SeatMaintenance sm where sm.seat.room.id = :roomId")
    List<SeatMaintenance> getSeatMaintenanceWithRoomId(@Param("roomId") Integer roomId);
}
