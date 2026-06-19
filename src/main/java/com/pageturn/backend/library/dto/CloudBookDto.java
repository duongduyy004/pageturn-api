package com.pageturn.backend.library.dto;

import java.time.Instant;

public record CloudBookDto(
        String bookHash,
        String title,
        String author,
        String fileFormat,
        String coverKey,
        String fileKey,
        long fileSize,
        boolean cloudSynced,
        Instant addedAt,
        Instant updatedAt
) {
}
