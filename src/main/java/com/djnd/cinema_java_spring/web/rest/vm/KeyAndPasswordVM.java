package com.djnd.cinema_java_spring.web.rest.vm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)

public class KeyAndPasswordVM {
    String key;
    String newPassword;
    String confirmPassword;
}
