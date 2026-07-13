package com.djnd.cinema_java_spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;
import com.djnd.cinema_java_spring.service.projection.PublishCustomerBookingProjection;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    /**
     * mix op and pess lock with version
     * FORCE_INCREMENT before end transaction it update version + 1 unit
     * 
     * @param bookingId
     * @return
     */
    // @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = { "bookingDetails", "bookingDetails.seat", "bookingDetails.showtime" })
    @Query(value = "select b from Booking b left join fetch b.customer c where b.id = :bookingId")
    Optional<Booking> findForUpdateDetailByIdWithVersion(@Param("bookingId") Long bookingId);

    @Modifying
    @Query(value = "update Booking b set b.status = :status where b.id in :bookingIds")
    void updateStatusByIdIn(@Param("status") BookingStatus status, @Param("bookingIds") List<Long> ids);

    @Query(value = "select b from Booking b left join b.customer c left join c.user u where b.bookingCode like concat('%', :q, '%') or u.phone like concat('%', :q, '%') or c.identityCard like concat('%', :q, '%')", countQuery = "select count(b) from Booking b join b.customer c join c.user u where b.bookingCode like concat('%', :q, '%') or u.phone like concat('%', :q, '%') or c.identityCard like concat('%', :q, '%')")
    Page<Booking> fetchAllWithPagination(@Param("q") String q, Pageable pageable);

    @Query(value = """
            select b.id as id, b.createdBy as createdBy, b.createdDate as createdDate,
                    b.lastModifiedBy as lastModifiedBy, b.lastModifiedDate as lastModifiedDate,
                            b.bookingCode as bookingCode, b.paymentMethod as paymentMethod,
                                    b.status as status, b.totalAmount as totalAmount, u.name as customerName,
                                            u.phone as customerPhone, c.identityCard as customerIdentityCard
            from Booking b
            left join b.customer c
            left join c.user u
            where b.id = :bookingId

                """)
    Optional<PublishCustomerBookingProjection> getPublishCustomerBookingDetailById(@Param("bookingId") Long bookingId);
}
