package com.djnd.cinema_java_spring.service.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleDTO {
    Integer id;
    @NotBlank(message = "Role name null!")
    String name;
    String description;
    Instant createdDate;
    Instant lastModifiedDate;
    List<PermissionDTO> permissions;

}
