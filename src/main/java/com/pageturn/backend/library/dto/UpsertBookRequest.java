package com.pageturn.backend.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpsertBookRequest(
        @NotBlank
        @Pattern(regexp = "^[a-f0-9]{64}$", message = "bookHash must be a SHA-256 hex string")
        String bookHash,
        @NotBlank
        @Size(max = 255)
        String title,
        @Size(max = 255)
        String author,
        @Pattern(regexp = "^(epub|pdf|txt)?$", message = "fileFormat must be epub, pdf, or txt")
        String fileFormat
) {
}
