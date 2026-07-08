package com.djnd.cinema_java_spring.service.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    @NotNull
    @JsonProperty("isActive")
    private boolean isActive;
    @NotNull
    private Integer quantity;
    @NotNull
    private LocalDateTime releaseDate;

}
