package com.pageturn.backend.common.api;

import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        int status,
        boolean success,
        String message,
        T data
) {
}
