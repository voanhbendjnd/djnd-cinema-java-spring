package com.djnd.cinema_java_spring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    boolean existsByShowtimeIdAndSeatIdIn(Long showtimeId, List<Integer> seatIds);

    @EntityGraph(attributePaths = { "showtime", "showtime.movie", "seat", "booking", "showtime.room" })
    @Query(value = "select t from Ticket t join t.booking b where b.customer.id = :customerId", countQuery = "select count(t) from Ticket t join t.booking b where b.customer.id = :customerId")
    Page<Ticket> getTicketsWithCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = { "showtime", "showtime.movie", "seat", "booking", "showtime.room" })
    @Query(value = "select t from Ticket t join t.booking b where b.customer.id = :customerId and t.id = :ticketId")
    Optional<Ticket> getTicketWithDetailByCustomerIdAndId(@Param("customerId") Long customerId,
            @Param("ticketId") Long ticketId);

    @Query(value = "select concat(s.seatRow, s.seatNo) from Ticket t join t.seat s where t.showtime.id = :showtimeId and t.seat.id in :seatIds")
    List<String> getSeatsPositionSold(@Param("showtimeId") Long showtimeId, @Param("seatIds") List<Integer> seatIds);

    @EntityGraph(attributePaths = { "showtime", "showtime.movie", "seat", "showtime.room" })
    @Query(value = "select t from Ticket t where t.booking.id = :bookingId")
    List<Ticket> getTicketsByBookingId(@Param("bookingId") Long bookingId);
    @EntityGraph(attributePaths = {"showtime","booking", "booking.customer"})
    @Query(value = "select t from Ticket t where t.id = :ticketId")
    Optional<Ticket> getTicketDetailBookingWithId(@Param("ticketId") Long ticketId);
    @Query(value = "select exists(select 1 from Ticket t where t.id = :ticketId and t.booking.customer.id = :customerId)")
    boolean existTicketByTicketIdAndCustomerId(@Param ("ticketId")Long ticketId,@Param("customerId") Long customerId);
    @EntityGraph(attributePaths = {"booking.customer.user", "showtime", "showtime.movie"})
    @Query(value = """
    select t from Ticket t
    where t.seat.id = :seatId
    and t.showtime.startDateTime between :startTimeMaintenance and :endTimeMaintenance
""")
    List<Ticket> getAllTicketCustomerAlreadyHasWithSeatMaintenanceAndTimeIn(@Param("seatId") Integer seatId, @Param("startTimeMaintenance") LocalDateTime startTimeMaintenance, @Param("endTimeMaintenance")LocalDateTime endTimeMaintenance);
}


}
