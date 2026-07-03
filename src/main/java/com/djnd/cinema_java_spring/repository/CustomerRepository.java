package com.djnd.cinema_java_spring.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.service.projection.AccountCustomerProjection;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    public static final String CACHE_INFORMATION_ACCOUNT_BY_USER_ID = "inforByUserId";

    @Cacheable(cacheNames = CACHE_INFORMATION_ACCOUNT_BY_USER_ID, unless = "#result == null")
    @Query("""
            select new com.djnd.cinema_java_spring.service.projection.AccountCustomerProjection(
                u.id,
                u.name,
                u.login,
                u.email,
                u.gender,
                u.phone,
                u.avatarUrl,
                u.activated,
                u.createdDate,
                u.createdBy,
                u.lastModifiedDate,
                u.lastModifiedBy,
                c.identityCard,
                c.address,
                c.loyaltyPoints

            )
            from User u
            join u.customer c
            where u.id = :userId
            """)
    Optional<AccountCustomerProjection> getInformationProfileUserById(@Param("userId") Long userId);
}
