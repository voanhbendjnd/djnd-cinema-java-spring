package com.djnd.cinema_java_spring.repository;

import java.util.List;
import java.util.Optional;

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
}
