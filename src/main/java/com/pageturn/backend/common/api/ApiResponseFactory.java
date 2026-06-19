package com.pageturn.backend.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(new ApiResponse<>(Instant.now(), status.value(), true, message, data));
    }

    public static ResponseEntity<ApiResponse<Void>> success(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse<>(Instant.now(), status.value(), true, message, null));
    }

    public static ResponseEntity<ApiResponse<Object>> error(HttpStatus status, String message, Object data) {
        return ResponseEntity.status(status).body(new ApiResponse<>(Instant.now(), status.value(), false, message, data));
    }
}
