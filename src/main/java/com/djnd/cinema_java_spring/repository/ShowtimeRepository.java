package com.djnd.cinema_java_spring.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Showtime;
import com.djnd.cinema_java_spring.domain.enumeration.ShowtimeStatus;
import com.djnd.cinema_java_spring.service.projection.ShowtimeProjection;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
        @Query(value = "select exists(select 1 from Showtime s where s.room.id = :roomId and :newStartDateTime < s.endDateTime and :newEndDateTime > s.startDateTime and s.movie.id <> :movieId)")
        boolean isRoomOccupied(@Param("roomId") Integer roomId,
                        @Param("newStartDateTime") LocalDateTime newStartDateTime,
                        @Param("newEndDateTime") LocalDateTime newEndDateTime,
                        @Param("movieId") Integer movieId);

        @EntityGraph(attributePaths = { "room" })
        List<Showtime> findByMovieId(Integer movieId);

        @Query(value = "select exists(select 1 from Showtime s join s.movie m where m.id = :movieId)")
        boolean movieHasHadScreenings(@Param("movieId") Integer movieId);

        @EntityGraph(attributePaths = { "room" })
        @Query(value = "select s from Showtime s where s.room.id in :roomIds and s.movie.id <> :movieId and s.startDateTime >= :start and s.endDateTime < :end")
        List<Showtime> findConflictShowtimes(@Param("roomIds") List<Integer> roomIds, @Param("movieId") Integer movieId,
                        @Param("start") LocalDateTime star, @Param("end") LocalDateTime end);

        void deleteByMovieIdAndRoomIdIn(Integer movieId, List<Integer> roomIds);

        @Query(value = "select s.startDateTime as startDateTime, s.endDateTime as endDateTime, m.title as title from Showtime s join s.movie m where s.room.id = :roomId and m.id <> :movieId and s.startDateTime between :startOfDay and :endOfDay order by s.startDateTime asc")
        List<ShowtimeProjection> getAllScheduleRoomAndMovieTitle(@Param("roomId") Integer roomId,
                        @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay,
                        @Param("movieId") Integer movieId);

        @Query(value = "select s from Showtime s where s.room.id = :roomId and s.startDateTime between :start and :end")
        List<Showtime> getAllShowtimesMovie(@Param("roomId") Integer roomId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        boolean existsByRoomId(Integer roomId);

        @EntityGraph(attributePaths = { "room" })
        @Query(value = "select s from Showtime s where s.startDateTime >= :start and s.startDateTime < :end and s.movie.id = :movieId and s.status = :status")
        List<Showtime> getAllShowtimeWithDay(@Param("movieId") Integer movieId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("status") ShowtimeStatus status);
}
