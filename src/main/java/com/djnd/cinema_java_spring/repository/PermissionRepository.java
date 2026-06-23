package com.djnd.cinema_java_spring.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Permission;
import com.djnd.cinema_java_spring.domain.enumeration.PermissionMethod;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    String USERS_PERMISSION_STRING_CACHE = "usersPermissionStringsById";
    String PERMISSIONS_BY_ROLE_ID = "permissionByRoleId";

    @Query(value = "select p from Permission p where p.name like concat('%', :q, '%')", countQuery = "select count(p) from Permission p where p.name like concat('%',:q ,'%')")
    Page<Permission> fetchAll(@Param("q") String q, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Integer id);

    boolean existsByMethodAndApiPath(PermissionMethod method, String apiPath);

    boolean existsByMethodAndApiPathAndIdNot(PermissionMethod method, String apiPath, Integer id);

    List<Permission> findByIdIn(List<Integer> permissionIds);

    @Cacheable(cacheNames = USERS_PERMISSION_STRING_CACHE, unless = "#result == null")
    @Query(value = "select concat(p.apiPath,':' ,p.method) from User u join u.role r join r.permissions p where u.id = :userId")
    Set<String> findPermissionStringsByUserId(@Param("userId") Long userId);

    @Cacheable(cacheNames = PERMISSIONS_BY_ROLE_ID, unless = "#result==null")
    @Query(value = "select concat(p.apiPath, ':', p.method) from Role r join r.permissions p where r.id = :roleId")
    Set<String> getPermissionByRoleId(@Param("roleId") Integer roleId);

    int countByIdIn(List<Integer> ids);

    @EntityGraph(attributePaths = { "roles" })
    @Query(value = "select p from Permission p where p.id = :permissionId")
    Optional<Permission> findWithDetailRoles(@Param("permissionId") Integer permissionId);

}
