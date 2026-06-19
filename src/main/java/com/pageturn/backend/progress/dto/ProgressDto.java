package com.pageturn.backend.progress.dto;

import java.time.Instant;

public record ProgressDto(
        String bookHash,
        int chapterIdx,
        double scrollPct,
        Instant updatedAt
) {
}
