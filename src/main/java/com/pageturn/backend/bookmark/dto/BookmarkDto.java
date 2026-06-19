package com.pageturn.backend.bookmark.dto;

import java.time.Instant;

public record BookmarkDto(
        Long id,
        String bookHash,
        int chapterIdx,
        double scrollPct,
        String snippet,
        Instant createdAt,
        Instant updatedAt,
        boolean isDeleted
) {
}
