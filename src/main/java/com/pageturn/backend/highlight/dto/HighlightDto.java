package com.pageturn.backend.highlight.dto;

import java.time.Instant;

public record HighlightDto(
        Long id,
        String bookHash,
        int chapterIdx,
        int startOffset,
        int endOffset,
        String textContent,
        String color,
        String note,
        Instant createdAt,
        Instant updatedAt,
        boolean isDeleted
) {
}
