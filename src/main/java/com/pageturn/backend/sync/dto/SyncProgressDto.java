package com.pageturn.backend.sync.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public record SyncProgressDto(
        @NotBlank
        @Pattern(regexp = "^[a-f0-9]{64}$")
        String bookHash,
        @Min(0)
        int chapterIdx,
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        double scrollPct,
        Instant updatedAt
) {
}
