package com.tech.project.AirbnbBackend.advice;

import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>>handleResourceNotFound(ResourceNotFoundException exception){
        ApiError apiError = ApiError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(exception.getLocalizedMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>>handleInternalServerError(Exception exception){
        ApiError apiError = ApiError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(exception.getLocalizedMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>>handleBadCredentialsException(BadCredentialsException badCredentialsException){
        ApiError apiError = ApiError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .message(badCredentialsException.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>>handleJwtException(JwtException jwtException){
        ApiError apiError = ApiError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .message(jwtException.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>>handleAccessDeniedException(AccessDeniedException accessDeniedException){
        ApiError apiError = ApiError.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .message(accessDeniedException.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        ApiError apiError = ApiError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message("Validation failed")
                .subErrors(errors)
                .build();

        return buildErrorResponseEntity(apiError);
    }


    private ResponseEntity<ApiResponse<?>> buildErrorResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(new ApiResponse<>(apiError),apiError.getHttpStatus());
    }
}
