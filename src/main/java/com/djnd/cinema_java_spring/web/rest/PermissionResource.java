package com.djnd.cinema_java_spring.web.rest;

import org.springframework.data.domain.Pageable;
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

import com.djnd.cinema_java_spring.domain.enumeration.PermissionMethod;
import com.djnd.cinema_java_spring.repository.PermissionRepository;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.PermissionService;
import com.djnd.cinema_java_spring.service.dto.PermissionDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PermissionResource {
    final PermissionService permissionService;
    final PermissionRepository permissionRepository;

    private static void isValidMethod(String method) {
        try {
            PermissionMethod.valueOf(method);
        } catch (Exception ex) {
            throw new RequestInvalidException("Permission method invalid!");
        }
    }

    @PostMapping
    @ApiMessage("Create new permission")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        if (permissionDTO.getId() != null) {
            throw new RequestInvalidException("A new permission cannot already has an ID");
        }
        if (permissionRepository.existsByName(permissionDTO.getName().toUpperCase())) {
            throw new RequestInvalidException("Permission name already exist!");
        }
        isValidMethod(permissionDTO.getMethod());
        if (permissionRepository.existsByMethodAndApiPath(PermissionMethod.valueOf(permissionDTO.getMethod()),
                permissionDTO.getApiPath().toLowerCase())) {
            throw new RequestInvalidException("Permission api path with method already exist!");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.createPermission(permissionDTO));

    }

    @PutMapping
    @ApiMessage("Update permission")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PermissionDTO> updatePermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        if (permissionDTO.getId() == null)
            throw new RequestInvalidException("Permission ID missing request!");
        if (permissionRepository.existsByNameAndIdNot(permissionDTO.getName().toUpperCase(), permissionDTO.getId())) {
            throw new RequestInvalidException("Permission name already exist!");
        }
        isValidMethod(permissionDTO.getMethod());
        if (permissionRepository.existsByMethodAndApiPathAndIdNot(PermissionMethod.valueOf(permissionDTO.getMethod()),
                permissionDTO.getApiPath().toLowerCase(), permissionDTO.getId())) {
            throw new RequestInvalidException("Permission api path with method already exist!");
        }
        return ResponseEntity.ok(permissionService.updatePermission(permissionDTO));

    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete permission by id")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deletePermission(@Positive @PathVariable(name = "id") Integer id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping
    @ApiMessage("Get all permission and find by name")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ResultPaginationDTO> fetchAll(Pageable pageable,
            @RequestParam(name = "q", required = false) String q) {
        return ResponseEntity.ok(permissionService.getAllPermissionWithPagination(pageable, q));
    }

}
