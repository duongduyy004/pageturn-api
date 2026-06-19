package com.pageturn.backend.progress.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SaveProgressRequest(
        @NotBlank
        @Pattern(regexp = "^[a-f0-9]{64}$")
        String bookHash,
        @Min(0)
        int chapterIdx,
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        double scrollPct
) {
}
