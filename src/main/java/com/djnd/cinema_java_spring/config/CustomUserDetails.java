package com.djnd.cinema_java_spring.config;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.djnd.cinema_java_spring.domain.entity.User;

public record CustomUserDetails(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authoritySet = new HashSet<SimpleGrantedAuthority>();
        if (user.getRole() != null) {
            var permissions = user.getRole().getPermissions();
            if (permissions != null) {
                authoritySet.add(new SimpleGrantedAuthority(user.getRole().getName()));
                permissions.stream().map(permission -> new SimpleGrantedAuthority(permission.getName()))
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
        if (user.getEmail() != null) {
            return user.getEmail();
        }
        return user.getLogin();

    }

}
