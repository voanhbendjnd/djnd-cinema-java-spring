package com.djnd.cinema_java_spring.domain.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "promotions")
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

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    // No-arg constructor
    public Promotion() {
    }

    // All-args constructor
    public Promotion(Long id, String title, String detail, Double discountPercentage,
                     LocalDateTime startTime, LocalDateTime endTime, String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.discountPercentage = discountPercentage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
