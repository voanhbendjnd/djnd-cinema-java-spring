package com.djnd.cinema_java_spring.security;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.djnd.cinema_java_spring.service.dto.UserSecurityCacheDTO;

public record CustomUserDetails(UserSecurityCacheDTO user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authoritySet = new HashSet<SimpleGrantedAuthority>();
        if (user.getRole() != null) {

            var permissions = user.getPermissions();
            if (permissions != null) {
                authoritySet.add(new SimpleGrantedAuthority(user.getRole()));
                permissions.stream().map(permission -> new SimpleGrantedAuthority(permission))
                        .forEach(authoritySet::add);
            }
        }
        return authoritySet;

    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        if (user.getLogin() != null) {
            return user.getLogin();

        }
        return user.getEmail();

    }

}
