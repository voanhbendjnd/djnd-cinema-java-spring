package com.djnd.cinema_java_spring.repository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.ShowtimePriceMatrix;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;

@Repository
public interface ShowtimePriceRepository extends JpaRepository<ShowtimePriceMatrix, Integer> {

        @Query(value = """
                        select exists (select 1 from ShowtimePriceMatrix s where s.dayType = :dayType and s.seatType = :seatType and s.startTimeFrom < :startTimeTo and s.startTimeTo > :startTimeFrom)
                        """)
        boolean existsByOverlapTime(@Param("dayType") String dayType, @Param("seatType") SeatType type,
                        @Param("startTimeFrom") LocalTime startTimeFrom, @Param("startTimeTo") LocalTime startTimeTo);

        @Query(value = """
                        select exists (select 1 from ShowtimePriceMatrix s where s.dayType = :dayType and s.seatType = :seatType and s.startTimeFrom < :startTimeTo and s.startTimeTo > :startTimeFrom and s.id <> :id)
                        """)
        boolean existsByOverlapTimeAndIdNot(@Param("id") Integer id, @Param("dayType") String dayType,
                        @Param("seatType") SeatType type,
                        @Param("startTimeFrom") LocalTime startTimeFrom, @Param("startTimeTo") LocalTime startTimeTo);

        /**
         * 
         * @param dayType
         * @param seatType
         * @param showtimeAt
         * @return final price
         */
        @Query(value = "select s.finalPrice from ShowtimePriceMatrix s where s.seatType = :seatType and :showtimeAt between s.startTimeFrom and s.startTimeTo")
        BigDecimal getFinalPriceByDayTypeAndSeatTypeAndBetweenAtShowtime(@Param("dayType") String dayType,
                        @Param("seatType") SeatType seatType, @Param("showtimeAt") LocalTime showtimeAt);

        @Query(value = "select s from ShowtimePriceMatrix s where s.dayType = :dayType and :showtimeAt between s.startTimeFrom and s.startTimeTo")
        List<ShowtimePriceMatrix> findByDayAndStartTime(@Param("dayType") String dayType,
                        @Param("showtimeAt") LocalTime showtimeAt);
}
