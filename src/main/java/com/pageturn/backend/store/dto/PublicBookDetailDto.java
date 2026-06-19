package com.pageturn.backend.store.dto;

import java.time.Instant;

public record PublicBookDetailDto(
        Long id,
        String bookHash,
        String title,
        String author,
        String description,
        String language,
        String coverKey,
        String fileKey,
        String fileFormat,
        long fileSize,
        String category,
        Long addedBy,
        long downloadCount,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
