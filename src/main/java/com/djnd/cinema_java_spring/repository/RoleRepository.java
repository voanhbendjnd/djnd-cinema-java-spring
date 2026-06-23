package com.djnd.cinema_java_spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Role;
import com.djnd.cinema_java_spring.service.projection.RoleUserProjection;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    String ROLES_BY_NAME_CACHE = "rolesByName";
    String ROLE_ID_BY_USER_ID_CACHE = "roleIdByUserId";

    @Cacheable(cacheNames = ROLES_BY_NAME_CACHE, unless = "#result == null")
    Optional<Role> findOneByName(String name);

    @EntityGraph(attributePaths = { "permissions" })
    @Query(value = "select r from Role r where r.id = :roleId")
    Optional<Role> findWithDetailById(@Param("roleId") Integer roleId);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Integer id);

    @EntityGraph(attributePaths = { "permissions" })
    @Query(value = "select r from Role r where r.name like concat('%', :q, '%')", countName = "select count(r) from Role r where r.name like concat('%', :q,'%')")
    Page<Role> fetchAllWithPagination(Pageable pageable, @Param("q") String q);

    @Query(value = "select r.id as id, r.name as name from Role r where r.name != :name")
    List<RoleUserProjection> fetchAllRole(@Param("name") String roleName);

}
