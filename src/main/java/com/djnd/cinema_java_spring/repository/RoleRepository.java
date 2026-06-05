package com.djnd.cinema_java_spring.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    String ROLES_BY_NAME_CACHE = "rolesByName";

    @Cacheable(cacheNames = ROLES_BY_NAME_CACHE, unless = "#result == null")
    Optional<Role> findOneByNameIgnoreCase(String name);
}
