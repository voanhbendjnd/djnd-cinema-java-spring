package com.djnd.cinema_java_spring.web.rest.errors;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.djnd.cinema_java_spring.service.dto.RestResponse;

@RestControllerAdvice
public class GlobalException {
    /**
     * not found
     * 
     * @param ex
     * @return json exception not found
     */
    @ExceptionHandler(value = { ResourceNotFoundException.class })
    public ResponseEntity<RestResponse<?>> handleObjectNotFoundException(ResourceNotFoundException ex) {
        int statusCode = HttpStatus.NOT_FOUND.value();
        var res = new RestResponse<>();
        res.setStatusCode(statusCode);
        res.setError("Not found!");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(statusCode).body(res);
    }

    /**
     * username already exist
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = { UsernameAlreadyUsedException.class })
    public ResponseEntity<RestResponse<?>> handleUsernameAlreadyUsedException(UsernameAlreadyUsedException ex) {
        int statusCode = HttpStatus.CONFLICT.value();
        var res = new RestResponse<>();
        res.setStatusCode(statusCode);
        res.setError("Conflict!");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(statusCode).body(res);
    }

    /**
     * user not logged in
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = { UnauthorizedException.class })
    public ResponseEntity<RestResponse<?>> handleUnthorizedException(UnauthorizedException ex) {
        int statusCode = HttpStatus.UNAUTHORIZED.value();
        var res = new RestResponse<>();
        res.setStatusCode(statusCode);
        res.setError("Unauthorized!");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(statusCode).body(res);
    }

    /**
     * user not have permission
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = { UserAccessDeniedException.class })
    public ResponseEntity<RestResponse<?>> handleUserAccessDeniedException(UserAccessDeniedException ex) {
        int statusCode = HttpStatus.FORBIDDEN.value();
        var res = new RestResponse<>();
        res.setStatusCode(statusCode);
        res.setError("Forbiden!");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(statusCode).body(res);
    }

    /**
     * bad request
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = { RequestInvalidException.class })
    public ResponseEntity<RestResponse<?>> handleBadRequestException(RequestInvalidException ex) {
        int statusCode = HttpStatus.BAD_REQUEST.value();
        var res = new RestResponse<>();
        res.setStatusCode(statusCode);
        res.setError("Bad request!");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(statusCode).body(res);
    }

    /**
     * Handle exception for valid request
     * 
     * @param ex
     * @return bad request 400
     **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        var res = new RestResponse<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {
                    errors.put(
                            error.getField(),
                            error.getDefaultMessage());
                });
        res.setError("Bad request data!");
        res.setMessage(errors);
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * bad request
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = { SeatOccupiedException.class })
    public ResponseEntity<RestResponse<?>> handleBadRequestException(SeatOccupiedException ex) {
        int statusCode = HttpStatus.BAD_REQUEST.value();
        var res = new RestResponse<>();
        res.setStatusCode(statusCode);
        res.setError("Bad request!");
        res.setMessage("Some of seats you chose have already been taken by others!");
        res.setData(ex.getOccupiedSeatIds());
        return ResponseEntity.status(statusCode).body(res);
    }

}
