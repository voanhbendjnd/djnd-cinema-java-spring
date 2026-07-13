package com.djnd.cinema_java_spring.web.rest.errors;

public class OperationCannotPerformedException extends RuntimeException{
    public OperationCannotPerformedException(String message){
        super(message);
    }
}
