package com.djnd.cinema_java_spring.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.service.projection.PublishUserProjection;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
        String USERS_BY_LOGIN_CACHE = "usersByLogin";
        String USERS_BY_EMAIL_CACHE = "usersByEmail";
        String USERS_BY_ID_CACHE = "usersById";

        List<User> findAllByActivatedIsFalseAndActivationKeyNotNullAndCreatedDateBefore(Instant createdDateBefore);

        Optional<User> findOneByLogin(String login);

        Optional<User> findOneByEmail(String email);

        Optional<User> findOneByEmailAndActivatedIsTrue(String email);

        // @Cacheable(cacheNames = USERS_BY_LOGIN_EMAIL_CACHE, key = "#login")
        @EntityGraph(attributePaths = { "role", "role.permissions" })
        Optional<User> findOneByLoginOrEmail(String login, String email);

        @EntityGraph(attributePaths = { "role", "role.permissions" })
        @Cacheable(cacheNames = USERS_BY_ID_CACHE, unless = "#result == null")
        @Query(value = "select u from User u where u.id = :userId")
        Optional<User> findWithDetailById(@Param("userId") Long userId);

        @Modifying
        @Query(value = "update User u set u.refreshToken = :refreshToken where u.id = :userId")
        int updateRefreshTokenById(@Param("userId") Long userId, @Param("refreshToken") String token);

        @Modifying
        @Query(value = "update User u set u.refreshToken = null where u.email = :email")
        @CacheEvict(cacheNames = USERS_BY_EMAIL_CACHE)
        int resetRefreshTokenByEmail(@Param("email") String email);

        @Modifying
        @Query(value = "update User u set u.refreshToken = null where u.login = :login")
        @CacheEvict(cacheNames = USERS_BY_LOGIN_CACHE)
        int resetRefreshTokenByLogin(@Param("login") String login);

        @Query(value = "select u.refreshToken from User u where u.id = :userId")
        String getRefreshTokenById(@Param("userId") Long userId);

        @Query(value = "select u from User u left join fetch u.role r left join fetch r.permissions p where u.email = :email")
        Optional<User> findOneWithAuthoritiesByEmail(@Param("email") String email);

        @Query(value = "select u from User u left join fetch u.role r left join fetch r.permissions p where u.login = :login")
        Optional<User> findOneWithAuthoritiesByLogin(@Param("login") String login);

        @Modifying
        @Query(value = "update User u set u.sessionId = :sessionId where u.id = :userId")
        int updateSessionById(@Param("userId") Long userId, @Param("sessionId") String sessionId);

        Optional<User> findOneByActivationKey(String key);

        @Query(value = "select exists(select 1 from User u where u.phone = :phone)")
        boolean userExistByPhone(@Param("phone") String phone);

        Optional<User> findOneByResetKey(String resetKey);

        boolean existsByEmailAndIdNot(String email, Long id);

        boolean existsByLoginAndIdNot(String login, Long id);

        boolean existsByPhoneAndIdNot(String phone, Long id);

        @Query(value = """
                                select u.id as id, u.login as login,
                                        u.email as email, u.gender as gender,
                                                u.phone as phone, u.createdDate as createdDate,
                                                        u.lastModifiedDate as lastModifiedDate,
                                                                u.createdBy as createdBy,
                                                                    u.lastModifiedBy as lastModifiedBy
                        from User u where u.email like concat('',:q, '%') or u.login like concat('',:q, '%')""", countQuery = "select count(*) from User u where u.email like concat('',:q, '%') or u.login like concat('',:q, '%')")
        Page<PublishUserProjection> fetchAllUser(Pageable pageable, @Param("q") String q);

        @Query(value = "select exists(select 1 from User u join u.role r where r.id = :roleId)")
        boolean existByRoleId(@Param("roleId") Integer roleId);
}
