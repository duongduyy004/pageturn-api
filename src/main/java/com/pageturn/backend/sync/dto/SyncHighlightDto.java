package com.pageturn.backend.sync.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record SyncHighlightDto(
        Long id,
        @Pattern(regexp = "^[a-f0-9]{64}$")
        String bookHash,
        @Min(0)
        int chapterIdx,
        @Min(0)
        int startOffset,
        @Min(0)
        int endOffset,
        @Size(max = 10000)
        String textContent,
        @Pattern(regexp = "^(yellow|green|red|purple)$", message = "color must be one of: yellow, green, red, purple")
        String color,
        @Size(max = 2000)
        String note,
        Instant updatedAt,
        boolean isDeleted
) {
}
