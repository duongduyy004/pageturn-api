package com.pageturn.backend.storage.dto;

import java.time.Instant;

public record StoredBookResponse(
        String bookHash,
        String fileName,
        String contentType,
        long fileSize,
        String storagePath,
        Instant createdAt,
        Instant updatedAt
) {
}
