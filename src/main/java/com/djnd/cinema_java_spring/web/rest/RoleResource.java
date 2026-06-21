package com.djnd.cinema_java_spring.web.rest;

import java.util.List;

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

import com.djnd.cinema_java_spring.repository.RoleRepository;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.RoleService;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.RoleDTO;
import com.djnd.cinema_java_spring.service.projection.RoleUserProjection;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/admin/roles")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RoleResource {
    final RoleService roleService;

    final RoleRepository roleRepository;

    @PostMapping
    @ApiMessage("Create new role with permission")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")") // get from security context
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        if (roleDTO.getId() != null) {
            throw new RequestInvalidException("A new role cannot already has an ID");
        }
        if (roleRepository.existsByName(roleDTO.getName().toUpperCase())) {
            throw new RequestInvalidException("Role already exist!");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(roleDTO));

    }

    @PutMapping
    @ApiMessage("Uppdate role")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")") // get from security context
    public ResponseEntity<RoleDTO> updateRole(@Valid @RequestBody RoleDTO roleDTO) {
        if (roleDTO.getId() == null) {
            throw new RequestInvalidException("Role ID missing or not found!");
        }
        if (roleRepository.existsByNameAndIdNot(roleDTO.getName(), roleDTO.getId())) {
            throw new RequestInvalidException("Role name already exist!");
        }
        return ResponseEntity.ok(roleService.updateRole(roleDTO));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete role by ID")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")") // get from security context
    public ResponseEntity<RoleDTO> deleteRole(@Positive @PathVariable(name = "id") Integer id) {
        if (id == null) {
            throw new RequestInvalidException("Role Id missing or not found!");
        }
        roleService.deleteRole(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch role by ID")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")") // get from security context
    public ResponseEntity<RoleDTO> fetchRole(@Positive @PathVariable(name = "id") Integer id) {
        if (id == null) {
            throw new RequestInvalidException("Role Id missing or not found!");
        }
        return ResponseEntity.ok(roleService.fecthRole(id));
    }

    @GetMapping
    @ApiMessage("Fetch all role with pagination")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")") // get from security context
    public ResponseEntity<ResultPaginationDTO> fetchAllRole(Pageable pageable,
            @RequestParam(name = "q", required = false) String q) {
        return ResponseEntity.ok(roleService.fetchAllWithPagination(pageable, q));
    }

    @GetMapping("/user")
    @ApiMessage("Fetch all role")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")") // get from security context
    public ResponseEntity<List<RoleUserProjection>> getAllRole() {
        return ResponseEntity.ok(roleService.getAllRole());
    }

}
