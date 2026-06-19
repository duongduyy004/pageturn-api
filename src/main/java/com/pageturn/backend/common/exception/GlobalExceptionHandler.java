package com.pageturn.backend.common.exception;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException exception) {
        return ApiResponseFactory.error(exception.getStatus(), exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ApiResponseFactory.error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException exception) {
        return ApiResponseFactory.error(HttpStatus.BAD_REQUEST, "Validation failed", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException exception) {
        return ApiResponseFactory.error(HttpStatus.FORBIDDEN, "Access denied", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException exception) {
        return ApiResponseFactory.error(HttpStatus.UNAUTHORIZED, "Authentication failed", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException exception) {
        return ApiResponseFactory.error(HttpStatus.BAD_REQUEST, exception.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception exception) {
        return ApiResponseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", null);
    }
}
