package com.djnd.cinema_java_spring.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Showtime;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    @Query(value = "select exists(select 1 from Showtime s where s.room.id = :roomId and :newStartDateTime < s.endDateTime and :newEndDateTime > s.startDateTime)")
    boolean isRoomOccupied(@Param("roomId") Integer roomId, @Param("newStartDateTime") LocalDateTime newStartDateTime,
            @Param("newEndDateTime") LocalDateTime newEndDateTime);
}
