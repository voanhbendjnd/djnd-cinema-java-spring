package com.djnd.cinema_java_spring.service.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO implements Serializable {
    @Serial
    private final static long serialVersionID = 1L;
    Integer id;
    @NotBlank(message = "Permission name null!")
    String name;
    @NotBlank(message = "Api path null!")
    String apiPath;
    @NotBlank(message = "Method null!")
    String method;
    @NotBlank(message = "Module null!")
    String module;
    Instant lastModifiedDate;
}
