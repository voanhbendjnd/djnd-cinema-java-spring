package com.djnd.cinema_java_spring.domain.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Promotion extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String title;

    @Column(name = "detail", columnDefinition = "LONGTEXT")
    private String detail;

    @Column(name = "discount_percentage", nullable = false)
    private Double discountPercentage;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    private LocalDateTime releaseDate;
    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @NotNull
    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CustomerVoucher> customerVouchers = new ArrayList<>();
    @OneToOne(mappedBy = "voucher", cascade = {CascadeType.MERGE, CascadeType.PERSIST })
    BookingVoucher bookingVoucher;

}
