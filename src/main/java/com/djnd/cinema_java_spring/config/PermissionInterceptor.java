package com.djnd.cinema_java_spring.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.djnd.cinema_java_spring.repository.PermissionRepository;
import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.security.SecurityUtils;
import com.djnd.cinema_java_spring.web.rest.errors.UnauthorizedException;
import com.djnd.cinema_java_spring.web.rest.errors.UserAccessDeniedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    final PermissionRepository permissionRepository;
    final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var apiPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String httpMethod = request.getMethod();
        var userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null)
            throw new UnauthorizedException("You are not logged in!");
        var roleId = userRepository.getRoleIdByUserId(userId);
        if (roleId == null)
            throw new UnauthorizedException("User ID not found!");
        var permissions = permissionRepository.getPermissionByRoleId(roleId);
        // var userPermissionStringsSet =
        // permissionRepository.findPermissionStringsByUserId(userId);
        if (permissions == null || permissions.isEmpty()) {
            throw new UserAccessDeniedException("You do not have permission!");
        }
        String requiredPermission = apiPath + ":" + httpMethod;
        if (!permissions.contains(requiredPermission)) {
            throw new UserAccessDeniedException("You do not have permission to access this API!");
        }
        return true;
    }

}
