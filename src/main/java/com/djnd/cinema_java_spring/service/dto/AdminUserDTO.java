package com.djnd.cinema_java_spring.service.dto;


import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserDTO extends UserDTO {
    Integer roleId;
    String activationKey;
    @Size(min = 2, max = 10)
    String langKey;
    boolean activated = false;
    String resetKey;

}
