package com.pageturn.backend.bookmark.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateBookmarkRequest(
        @Min(0)
        int chapterIdx,
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        double scrollPct,
        @Size(max = 2000)
        String snippet
) {
}
