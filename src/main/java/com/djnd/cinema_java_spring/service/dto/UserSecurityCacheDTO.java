package com.djnd.cinema_java_spring.service.dto;

import java.io.Serializable;
import java.util.Set;

import com.djnd.cinema_java_spring.domain.enumeration.LoginWith;
import com.djnd.cinema_java_spring.domain.enumeration.UserGender;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurityCacheDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    Long id;
    String login;
    String email;
    String name;
    LoginWith loginWith;
    UserGender gender;
    boolean activated;
    String password;
    String langKey;
    String role;
    Set<String> permissions;
}
