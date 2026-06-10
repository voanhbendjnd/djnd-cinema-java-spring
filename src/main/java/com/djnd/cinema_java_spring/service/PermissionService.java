package com.djnd.cinema_java_spring.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Permission;
import com.djnd.cinema_java_spring.domain.enumeration.PermissionMethod;
import com.djnd.cinema_java_spring.repository.PermissionRepository;
import com.djnd.cinema_java_spring.service.dto.PermissionDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;
import org.hibernate.SessionFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class PermissionService {
    final PermissionRepository permissionRepository;
    final SessionFactory sessionFactory;

    public PermissionDTO createPermission(PermissionDTO perDTO) {
        var newPermision = Permission.builder()
                .apiPath(perDTO.getApiPath().toLowerCase())
                .method(PermissionMethod.valueOf(perDTO.getMethod()))
                .name(perDTO.getName().toUpperCase())
                .module(perDTO.getModule().toLowerCase())
                .build();
        return (this.toPermissionDTO(permissionRepository.save(newPermision)));

    }

    public PermissionDTO updatePermission(PermissionDTO perDTO) {

        var permission = permissionRepository.findById(perDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found!"));
        permission.setApiPath(perDTO.getApiPath().toLowerCase());
        permission.setMethod(PermissionMethod.valueOf(perDTO.getMethod()));
        permission.setName(perDTO.getName().toUpperCase());
        permission.setModule(perDTO.getModule().toLowerCase());
        var savePermission = permissionRepository.save(permission);
        sessionFactory.getCache().evictEntityData(Permission.class, savePermission.getId());
        return this.toPermissionDTO(savePermission);

    }

    public void deletePermission(Integer id) {
        var permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found!"));
        permissionRepository.delete(permission);
        sessionFactory.getCache().evictEntityData(Permission.class, id);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllPermissionWithPagination(Pageable pageable, String q) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = permissionRepository.fetchAll(q != null ? q.toUpperCase() : "", pageable);
        meta.setPage(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::toPermissionDTO).toList());
        return res;
    }

    public PermissionDTO toPermissionDTO(Permission permission) {
        return PermissionDTO.builder()
                .id(permission.getId())
                .apiPath(permission.getApiPath())
                .name(permission.getName())
                .module(permission.getModule())
                .method(permission.getMethod().toString())
                .build();
    }

}
