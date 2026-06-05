package com.djnd.cinema_java_spring.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    String USERS_BY_LOGIN_CACHE = "usersByLogin";
    String USERS_BY_EMAIL_CACHE = "usersByEmail";
    String USERS_BY_LOGIN_EMAIL_CACHE = "usersByLoginOrEmail";
    String USERS_BY_ID_CACHE = "usersById";
    String USERS_PERMISSION_STRING_CACHE = "usersPermissionStringsById";

    List<User> findAllByActivatedIsFalseAndActivationKeyNotNullAndCreatedDateBefore(Instant createdDateBefore);

    Optional<User> findOneByLogin(String login);

    Optional<User> findOneByEmail(String email);

    // @Cacheable(cacheNames = USERS_BY_LOGIN_EMAIL_CACHE, key = "#login")
    @EntityGraph(attributePaths = { "role", "role.permissions" })
    Optional<User> findOneByLoginOrEmail(String login, String email);

    @EntityGraph(attributePaths = { "role", "role.permissions" })
    @Cacheable(cacheNames = USERS_BY_ID_CACHE, unless = "#result == null")
    @Query(value = "select u from User u where u.id = :userId")
    Optional<User> findWithDetailById(@Param("userId") Long userId);

    @Cacheable(cacheNames = USERS_PERMISSION_STRING_CACHE, unless = "#result == null")
    @Query(value = "select concat(p.apiPath,':' ,p.method) from User u join u.role r join r.permissions p where u.id = :userId")
    Set<String> findPermissionStringsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "update User u set u.refreshToken = :refreshToken where u.id = :userId")
    int updateRefreshTokenById(@Param("userId") Long userId, @Param("refreshToken") String token);

    @Query(value = "select u.refreshToken from User u where u.id = :userId")
    String getRefreshTokenById(@Param("userId") Long userId);
}
