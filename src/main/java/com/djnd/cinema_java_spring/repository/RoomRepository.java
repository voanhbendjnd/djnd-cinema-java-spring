package com.djnd.cinema_java_spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Room;
import com.djnd.cinema_java_spring.service.projection.RoomNameProjection;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    @Query(value = "select r from Room r where lower(r.name) like lower(concat('%', :q, '%'))", countQuery = "select count(r) from Room r where lower(r.name) like lower(concat('%', :q, '%'))")
    Page<Room> fetchAllWithPagination(@Param("q") String q, Pageable pageable);

    @Query(value = "select exists(select 1 from Room r where lower(r.name) = lower(:name))")
    boolean roomNameIsExist(@Param("name") String name);

    @EntityGraph(attributePaths = { "seats" })
    @Query(value = "select r from Room r where r.id = :roomId")
    Optional<Room> findWithDetailSeatById(@Param("roomId") Integer roomId);

    int countByIdIn(List<Integer> ids);

    List<Room> findByIdIn(List<Integer> ids);

    @Query(value = "select r.id as is, r.name as name from Room r")
    List<RoomNameProjection> findAllRoomAvailable();
}
