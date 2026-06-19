package com.pageturn.backend.highlight.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateHighlightRequest(
        @Min(0)
        int chapterIdx,
        @Min(0)
        int startOffset,
        @Min(0)
        int endOffset,
        @NotBlank
        @Size(max = 10000)
        String textContent,
        @Pattern(regexp = "^(yellow|green|red|purple)$", message = "color must be one of: yellow, green, red, purple")
        String color,
        @Size(max = 2000)
        String note
) {
}
