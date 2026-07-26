package com.djnd.cinema_java_spring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.djnd.cinema_java_spring.service.projection.OccupancyMovieDetailProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Movie;
import com.djnd.cinema_java_spring.domain.enumeration.MovieStatus;
import com.djnd.cinema_java_spring.service.projection.PublishMovieProjection;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    @Query(value = "select m from Movie m where lower(m.title) like lower(concat('%', :q, '%'))", countQuery = "select count(m) from Movie m where lower(m.title) like lower(concat('%', :q, '%'))")
    Page<Movie> fetchAllWithPagination(@Param("q") String q, Pageable pageable);

    @EntityGraph(attributePaths = { "showtimes", "showtimes.room" })
    @Query(value = "select m from Movie m where m.id = :movieId")
    Optional<Movie> findWithDetailById(@Param("movieId") Integer movieId);

    @Modifying
    @Query(value = "update Movie m set m.posterUrl = :posterUrl where m.id = :movieId")
    int updatePosterUrl(@Param("movieId") Integer movieId, @Param("posterUrl") String posterUrl);

    @Query(value = "select m.id as id, m.title as title, m.posterUrl as posterUrl, m.genre as genre, m.releaseDate as releaseDate, m.sold as sold, m.status as status"
            +
            " from Movie m where m.status = :status")
    List<PublishMovieProjection> getPublishMovie(@Param("status") MovieStatus status);

    @Query("""
                select distinct m
                from Movie m
                left join fetch m.showtimes s
                left join fetch s.room
                where m.id = :id
            """)
    Optional<Movie> findDetailById(@Param("id") Integer id);

    @Query(value = """

            select distinct m.id as movieId, m.title as movieTitle, m.poster_url as posterUrl
    , sum(b.total_amount) as totalRevenue
    , count(t.id) as ticketsSold
    , (select count(st1.id) from showtimes st1 where st1.movie_id = m.id and st1.status = 'ACTIVE') as totalShowtimes
                ,ROUND(
                         COUNT(DISTINCT t.id) * 100.0 /
                         NULLIF(
                             (
                                 SELECT SUM(seat_count)
                                 FROM (
                                     SELECT 
                                            COUNT(s.id) AS seat_count
                                     FROM showtimes st2
                                     JOIN rooms r ON r.id = st2.room_id
                                     JOIN seats s ON s.room_id = r.id
                                     WHERE st2.movie_id = m.id
                                     GROUP BY st2.id
                                 ) as x
                             ),
                             0
                         ),
                         2
                     ) AS occupancyRate
    from movies m
    join showtimes st on m.id= st.movie_id
            join rooms r on r.id = st.room_id
    join booking_detail bd on bd.showtime_id = st.id
    join bookings b on bd.booking_id = b.id
    join tickets t on t.booking_id = b.id
    where
    b.created_date >= :fromDateTime and b.created_date < :toDateTime
    and m.status = 'SHOWING'
    group by m.id, m.title, m.poster_url
    order by totalRevenue desc
    limit :limit
    """, nativeQuery = true)
    List<OccupancyMovieDetailProjection> getTopPerformingMovies(@Param("fromDateTime")LocalDateTime fromDateTime, @Param("toDateTime") LocalDateTime toDateTime, @Param("limit") int limit);
}
