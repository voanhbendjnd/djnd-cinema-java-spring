package com.djnd.cinema_java_spring.service.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PromotionDTO {

    private Long id;

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String detail;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount percentage must be > 0, please re-enter")
    @DecimalMax(value = "100.0", inclusive = true, message = "Discount percentage must be <= 100")
    private Double discountPercentage;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private String thumbnailUrl;

    // Status is not persisted; computed dynamically
    private String status;

    // No-arg constructor
    public PromotionDTO() {
    }

    // All-args constructor
    public PromotionDTO(Long id, String title, String detail, Double discountPercentage,
                        LocalDateTime startTime, LocalDateTime endTime, String thumbnailUrl, String status) {
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.discountPercentage = discountPercentage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
