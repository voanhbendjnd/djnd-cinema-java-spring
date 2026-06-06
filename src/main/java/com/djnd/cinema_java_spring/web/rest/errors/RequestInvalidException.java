package com.djnd.cinema_java_spring.web.rest.errors;

import java.io.Serial;

public class RequestInvalidException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public RequestInvalidException(String message) {
        super(message);
    }

}
