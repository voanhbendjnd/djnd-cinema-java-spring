package com.djnd.cinema_java_spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Permission;
import com.djnd.cinema_java_spring.domain.enumeration.PermissionMethod;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    @Query(value = "select p from Permission p where p.name like concat('%', :q, '%')", countQuery = "select count(*) from Permission p where p.name like concat('%',:q ,'%')")
    Page<Permission> fetchAll(@Param("q") String q, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Integer id);

    boolean existsByMethodAndApiPath(PermissionMethod method, String apiPath);

    boolean existsByMethodAndApiPathAndIdNot(PermissionMethod method, String apiPath, Integer id);

}
