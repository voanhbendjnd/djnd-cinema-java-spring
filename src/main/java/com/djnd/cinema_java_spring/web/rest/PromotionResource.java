package com.djnd.cinema_java_spring.web.rest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.PromotionService;
import com.djnd.cinema_java_spring.service.dto.PromotionDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/promotions")
public class PromotionResource {

    private final PromotionService promotionService;

    public PromotionResource(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * GET /api/promotions
     * Paginated list, default sort startTime DESC.
     */
    @GetMapping
    @ApiMessage("Fetch all promotions with pagination")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "q", required = false) String q) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "startTime"));
        return ResponseEntity.ok(promotionService.fetchAll(pageable, q));
    }

    /**
     * POST /api/promotions
     * Create a new promotion.
     */
    @PostMapping
    @ApiMessage("Create new promotion")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PromotionDTO> create(@Valid @RequestBody PromotionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(dto));
    }

    /**
     * GET /api/promotions/{id}
     * Fetch promotion detail or 404.
     */
    @GetMapping("/{id}")
    @ApiMessage("Fetch promotion by ID")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PromotionDTO> fetchById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.fetchById(id));
    }

    /**
     * PUT /api/promotions/{id}
     * Update promotion. Blocks startTime change if campaign has started.
     */
    @PutMapping("/{id}")
    @ApiMessage("Update promotion")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PromotionDTO> update(@PathVariable Long id, @RequestBody PromotionDTO dto) {
        return ResponseEntity.ok(promotionService.update(id, dto));
    }

    /**
     * DELETE /api/promotions/{id}
     * Hard-blocks deletion of Active promotions.
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Delete promotion")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return ResponseEntity.ok(null);
    }
}
