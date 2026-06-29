package com.djnd.cinema_java_spring.service;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query(value = "select exists(select 1 from Ticket t where t.showtime.id = :showtimeId and t.seat.id in :seatIds)")
    boolean existByShowtimeIdAndSeatIdIn(@Param("showtimeId") Long showtimeId, @Param("seatIds") List<Integer> seatIds);

    @EntityGraph(attributePaths = { "seat" })
    @Query(value = "select t from Ticket t where t.showtime.id = :showtimeId")
    List<Ticket> findByShowtimeId(@Param("showtimeId") Long showtimeId);
}
