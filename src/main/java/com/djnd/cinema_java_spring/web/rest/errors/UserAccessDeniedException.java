package com.djnd.cinema_java_spring.web.rest.errors;

import java.io.Serial;

public class UserAccessDeniedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserAccessDeniedException(String message) {
        super(message);
    }

}
