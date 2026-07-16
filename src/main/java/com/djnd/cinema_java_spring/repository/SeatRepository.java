package com.djnd.cinema_java_spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.service.projection.SeatLayoutDTO;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    @Query(value = """
            select s.id as id, s.seatRow as seatRow, s.seatNo as seatNo,
            s.type as type, s.status as status,
            (case when t.id is not null then 'SOLD' else 'AVAILABLE' end)
            from Seat s
            left join Ticket t
            on t.seat.id = s.id
            and t.showtime.id = :showtimeId
            where s.room.id = (select sh.room.id from Showtime sh where sh.id = :showtimeId)
            order by s.seatRow asc, s.seatNo asc
            """)
    List<SeatLayoutDTO> getSeatLayoutByShowtime(@Param("showtimeId") Long showtimeId);

    List<Seat> findByIdIn(List<Integer> seatIds);
    @Query(value = "select exists(select 1 from Seat s where s.id = :seatId)")
    boolean existById(@Param("seatId") Integer seatId);
}
