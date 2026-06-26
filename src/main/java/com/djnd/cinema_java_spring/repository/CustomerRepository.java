package com.djnd.cinema_java_spring.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Customer;
import com.djnd.cinema_java_spring.service.projection.AccountCustomerProjection;
import com.djnd.cinema_java_spring.service.projection.ProfileUserProjection;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    public static final String CACHE_INFORMATION_ACCOUNT_BY_USER_ID = "inforByUserId";

    @Cacheable(cacheNames = CACHE_INFORMATION_ACCOUNT_BY_USER_ID, unless = "#result == null")
    @Query(value = """
                         select u.id as id, u.name as name, u.login as login, u.email as email,
                                         u.gender as gender, u.phone as phone, u.avatarUrl as avatarUrl, u.activated as activated,
                                                         u.createdDate as createdDate, u.createdBy as createdBy,
                                                                         u.lastModifiedDate as lastModifiedDate, u.lastModifiedBy as lastModifiedBy,
                                                                                         c.identityCard as identityCard, c.address as address,
                                                                                                         c.loyaltyPoints as loyaltyPoints, c.dateOfBirth as dateOfBirth
            from User u
                         join u.customer c
                         where u.id = :userId
                         """)
    Optional<AccountCustomerProjection> getInformationProfileUserById(@Param("userId") Long userId);
}
