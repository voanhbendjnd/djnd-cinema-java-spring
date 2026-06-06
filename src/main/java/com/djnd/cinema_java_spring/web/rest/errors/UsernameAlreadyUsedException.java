package com.djnd.cinema_java_spring.web.rest.errors;

import java.io.Serial;

public class UsernameAlreadyUsedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UsernameAlreadyUsedException(String msg) {
        super(msg);
    }
}
