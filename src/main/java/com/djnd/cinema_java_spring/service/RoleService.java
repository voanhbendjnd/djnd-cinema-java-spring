package com.djnd.cinema_java_spring.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Permission;
import com.djnd.cinema_java_spring.domain.entity.Role;
import com.djnd.cinema_java_spring.repository.PermissionRepository;
import com.djnd.cinema_java_spring.repository.RoleRepository;
import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.service.dto.PermissionDTO;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.RoleDTO;
import com.djnd.cinema_java_spring.service.projection.RoleUserProjection;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class RoleService {
    final RoleRepository roleRepository;
    final PermissionRepository permissionRepository;
    final CacheManager cacheManager;
    final UserRepository userRepository;
    final EntityManager entityManager;

    public RoleDTO createRole(RoleDTO roleDTO) {
        var role = new Role();
        role.setName(roleDTO.getName().toUpperCase());
        role.setDescription(roleDTO.getDescription());
        if (roleDTO.getPermissions() != null && !roleDTO.getPermissions().isEmpty()) {
            int count = permissionRepository.countByIdIn(
                    roleDTO.getPermissions().stream().map(PermissionDTO::getId).toList());
            if (count != roleDTO.getPermissions().size()) {
                throw new RequestInvalidException("Permission ID invalid!");
            }
            List<Permission> proxies = roleDTO.getPermissions().stream()
                    .map(per -> entityManager
                            .getReference(Permission.class, per.getId()))
                    .toList();
            role.setPermissions(proxies);
        } else {
            role.setPermissions(new ArrayList<>());
        }
        roleRepository.save(role);
        return toRoleDTO(role);
    }

    public RoleDTO updateRole(RoleDTO roleDTO) {
        var role = roleRepository.findById(roleDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        if (roleDTO.getPermissions() != null && !roleDTO.getPermissions().isEmpty()) {

            int count = permissionRepository
                    .countByIdIn(roleDTO.getPermissions().stream().map(PermissionDTO::getId).toList());
            if (count != role.getPermissions().size()) {
                throw new RequestInvalidException("Permission ID invalid!");
            }
            List<Permission> proxies = roleDTO.getPermissions().stream()
                    .map(per -> entityManager.getReference(Permission.class, per.getId())).toList();
            role.setPermissions(proxies);
        } else {
            role.setPermissions(new ArrayList<>());
        }
        roleRepository.save(role);
        this.clearCacheRole(role);
        return toRoleDTO(role);
    }

    public void deleteRole(Integer roleId) {
        var role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
        if (userRepository.existByRoleId(roleId)) {
            throw new RequestInvalidException("Current role already has user used!");
        }
        roleRepository.delete(role);
        this.clearCacheRole(role);
    }

    @Transactional(readOnly = true)
    public List<RoleUserProjection> getAllRole() {
        return roleRepository.fetchAllRole();
    }

    @Transactional(readOnly = true)
    public RoleDTO fecthRole(Integer id) {
        var role = roleRepository.findWithDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
        return toRoleDTO(role);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO fetchAllWithPagination(Pageable pageable, String q) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = roleRepository.fetchAllWithPagination(pageable, q != null ? q.toUpperCase() : "");
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::toRoleDTO).toList());
        return res;
    }

    private void clearCacheRole(Role role) {
        var cache = cacheManager.getCache(RoleRepository.ROLES_BY_NAME_CACHE);
        if (cache != null) {
            cache.evictIfPresent(role.getName());
        }
    }

    public RoleDTO toRoleDTO(Role role) {
        var permissions = role
                .getPermissions().stream().map(x -> PermissionDTO.builder().id(x.getId()).name(x.getName())
                        .apiPath(x.getApiPath()).method(x.getMethod().toString()).module(x.getModule()).build())
                .toList();
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions)
                .build();

    }
}
