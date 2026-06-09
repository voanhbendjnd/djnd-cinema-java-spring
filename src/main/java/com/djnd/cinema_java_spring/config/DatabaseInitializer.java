package com.djnd.cinema_java_spring.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.djnd.cinema_java_spring.domain.entity.Permission;
import com.djnd.cinema_java_spring.domain.entity.Role;
import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.domain.enumeration.LoginWith;
import com.djnd.cinema_java_spring.domain.enumeration.PermissionMethod;
import com.djnd.cinema_java_spring.domain.enumeration.UserGender;
import com.djnd.cinema_java_spring.repository.PermissionRepository;
import com.djnd.cinema_java_spring.repository.RoleRepository;
import com.djnd.cinema_java_spring.repository.UserRepository;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {
        final UserRepository userRepoRepository;
        final PermissionRepository permissionRepository;
        final RoleRepository roleRepository;
        final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) throws Exception {
                System.out.println(">>> START INIT DATABASE <<<");
                Long permissionCnt = this.permissionRepository.count();
                Long userCnt = this.userRepoRepository.count();
                Long roleCnt = this.roleRepository.count();
                if (permissionCnt == 0) {
                        List<Permission> permissionList = new ArrayList<>();
                        permissionList.add(
                                        new Permission("CREATE_PERMISSION",
                                                        Constants.VERSION_API + "/admin/permissions",
                                                        PermissionMethod.POST,
                                                        "PERMISSIONS"));
                        permissionList.add(
                                        new Permission("UPDATE_PERMISSION",
                                                        Constants.VERSION_API + "/admin/permissions",
                                                        PermissionMethod.PUT,
                                                        "PERMISSIONS"));
                        permissionList
                                        .add(new Permission("DELETE_PERMISSION",
                                                        Constants.VERSION_API + "/admin/permissions/admin/{id}",
                                                        PermissionMethod.DELETE,
                                                        "PERMISSIONS"));
                        permissionList.add(
                                        new Permission("GET_PERMISSION",
                                                        Constants.VERSION_API + "/admin/permissions{id}",
                                                        PermissionMethod.GET,
                                                        "PERMISSIONS"));
                        permissionList.add(new Permission("GET_ALL_PERMISSION",
                                        Constants.VERSION_API + "/admin/permissions",
                                        PermissionMethod.GET, "PERMISSIONS"));

                        permissionList.add(
                                        new Permission("CREATE_ROLE", Constants.VERSION_API + "/admin/roles",
                                                        PermissionMethod.POST,
                                                        "ROLES"));
                        permissionList.add(
                                        new Permission("UPDATE_ROLE", Constants.VERSION_API + "/admin/roles",
                                                        PermissionMethod.PUT,
                                                        "ROLES"));
                        permissionList.add(
                                        new Permission("DELETE_ROLE", Constants.VERSION_API + "/admin/roles/admin/{id}",
                                                        PermissionMethod.DELETE, "ROLES"));
                        permissionList.add(
                                        new Permission("GET_ROLE", Constants.VERSION_API + "/admin/roles/admin/{id}",
                                                        PermissionMethod.GET,
                                                        "ROLES"));
                        permissionList.add(
                                        new Permission("GET_ALL_ROLE", Constants.VERSION_API + "/admin/roles",
                                                        PermissionMethod.GET,
                                                        "ROLES"));

                        permissionList.add(
                                        new Permission("CREATE_USER", Constants.VERSION_API + "/admin/users",
                                                        PermissionMethod.POST,
                                                        "USERS"));
                        permissionList.add(
                                        new Permission("UPDATE_USER", Constants.VERSION_API + "/admin/users",
                                                        PermissionMethod.PUT,
                                                        "USERS"));
                        permissionList.add(
                                        new Permission("DELETE_USER", Constants.VERSION_API + "/admin/users/admin/{id}",
                                                        PermissionMethod.DELETE, "USERS"));
                        permissionList.add(
                                        new Permission("GET_USER", Constants.VERSION_API + "/admin/users/admin/{id}",
                                                        PermissionMethod.GET,
                                                        "USERS"));
                        permissionList.add(
                                        new Permission("GET_ALL_USER", Constants.VERSION_API + "/admin/users",
                                                        PermissionMethod.GET,
                                                        "USERS"));
                        permissionList.add(
                                        new Permission("DELETE_USER_BY_LOGIN",
                                                        Constants.VERSION_API + "/admin/users/{login}",
                                                        PermissionMethod.DELETE,
                                                        "USERS"));

                        this.permissionRepository.saveAll(permissionList);
                }
                if (roleCnt == 0) {
                        var permissions = this.permissionRepository.findAll();
                        var roleAdmin = new Role();
                        var roles = new ArrayList<Role>();
                        roleAdmin.setName(AuthoritiesConstants.ADMIN);
                        roleAdmin.setDescription("SUPER ADMIN HAS FULL PERMISSIONS");
                        roleAdmin.setPermissions(permissions);
                        var roleUser = new Role();
                        roleUser.setName(AuthoritiesConstants.CUSTOMER);
                        roleUser.setDescription("CUSTOMER USE APP");
                        roleUser.setPermissions(permissions);
                        roles.add(roleUser);
                        roles.add(roleAdmin);
                        this.roleRepository.saveAll(roles);
                }
                if (userCnt == 0) {
                        User admin = new User();
                        admin.setName("ADMIN");
                        admin.setLogin("admin");
                        admin.setActivated(true);
                        admin.setGender(UserGender.MALE);
                        admin.setPhone("0981550653");
                        admin.setEmail("benva.ce190709@gmail.com");
                        var roleAdmin = this.roleRepository.findOneByName(AuthoritiesConstants.ADMIN)
                                        .orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
                        admin.setRole(roleAdmin);
                        admin.setPassword(this.passwordEncoder.encode("123123"));
                        admin.setLoginWith(LoginWith.SYSTEM);
                        this.userRepoRepository.save(admin);
                }
                if (permissionCnt != 0 && roleCnt != 0 && userCnt != 0) {
                        System.out.println(">>> SKIP PROCESSING INITIALIZE <<<");
                } else {
                        System.out.println(">>> INIT DATABASE SUCCESSFULLY");
                }
        }
}
