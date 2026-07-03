package com.djnd.cinema_java_spring.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Promotion;
import com.djnd.cinema_java_spring.repository.PromotionRepository;
import com.djnd.cinema_java_spring.service.dto.PromotionDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

@Service
@Transactional
public class PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionService.class);

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    // -------------------------------------------------------
    // Helper: compute status dynamically
    // -------------------------------------------------------
    private String computeStatus(Promotion p) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(p.getStartTime())) {
            return "Upcoming";
        } else if (now.isAfter(p.getEndTime())) {
            return "Expired";
        } else {
            return "Active";
        }
    }

    // -------------------------------------------------------
    // Helper: map entity → DTO
    // -------------------------------------------------------
    private PromotionDTO toDTO(Promotion p) {
        PromotionDTO dto = new PromotionDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDetail(p.getDetail());
        dto.setDiscountPercentage(p.getDiscountPercentage());
        dto.setStartTime(p.getStartTime());
        dto.setEndTime(p.getEndTime());
        dto.setThumbnailUrl(p.getThumbnailUrl());
        dto.setStatus(computeStatus(p));
        return dto;
    }

    // -------------------------------------------------------
    // GET /api/promotions (paginated)
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public ResultPaginationDTO fetchAll(Pageable pageable, String q) {
        Page<Promotion> page = promotionRepository.fetchAllWithPagination(pageable, q != null ? q : "");

        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::toDTO).toList());
        return res;
    }

    // -------------------------------------------------------
    // POST /api/promotions
    // -------------------------------------------------------
    public PromotionDTO create(PromotionDTO dto) {
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().isEqual(dto.getEndTime())) {
            throw new RequestInvalidException("startTime must be before endTime");
        }

        if (promotionRepository.existsByTitleIgnoreCase(dto.getTitle())) {
            throw new RequestInvalidException("Duplicate promo code");
        }

        Promotion promotion = new Promotion();
        promotion.setTitle(dto.getTitle());
        promotion.setDetail(dto.getDetail());
        promotion.setDiscountPercentage(dto.getDiscountPercentage());
        promotion.setStartTime(dto.getStartTime());
        promotion.setEndTime(dto.getEndTime());
        promotion.setThumbnailUrl(dto.getThumbnailUrl());

        return toDTO(promotionRepository.save(promotion));
    }

    // -------------------------------------------------------
    // GET /api/promotions/{id}
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public PromotionDTO fetchById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));
        return toDTO(promotion);
    }

    // -------------------------------------------------------
    // PUT /api/promotions/{id}
    // -------------------------------------------------------
    public PromotionDTO update(Long id, PromotionDTO dto) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        if (dto.getTitle() != null && !dto.getTitle().equalsIgnoreCase(promotion.getTitle())) {
            if (promotionRepository.existsByTitleIgnoreCaseAndIdNot(dto.getTitle(), id)) {
                throw new RequestInvalidException("Mã khuyến mãi bị trùng");
            }
        }

        // Block startTime modification if campaign has started
        LocalDateTime now = LocalDateTime.now();
        if (dto.getStartTime() != null && !dto.getStartTime().equals(promotion.getStartTime())) {
            if (!promotion.getStartTime().isAfter(now)) {
                throw new RequestInvalidException(
                        "Cannot modify startTime: the campaign has already started or is ongoing");
            }
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                throw new RequestInvalidException("startTime must be before endTime");
            }
        }

        if (dto.getTitle() != null)
            promotion.setTitle(dto.getTitle());
        if (dto.getDetail() != null)
            promotion.setDetail(dto.getDetail());
        if (dto.getDiscountPercentage() != null)
            promotion.setDiscountPercentage(dto.getDiscountPercentage());
        if (dto.getStartTime() != null)
            promotion.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null)
            promotion.setEndTime(dto.getEndTime());
        if (dto.getThumbnailUrl() != null)
            promotion.setThumbnailUrl(dto.getThumbnailUrl());

        return toDTO(promotionRepository.save(promotion));
    }

    // -------------------------------------------------------
    // DELETE /api/promotions/{id}
    // -------------------------------------------------------
    public void delete(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        if ("Active".equals(computeStatus(promotion))) {
            throw new RequestInvalidException("Active promotions cannot be deleted");
        }

        promotionRepository.delete(promotion);
    }
}
