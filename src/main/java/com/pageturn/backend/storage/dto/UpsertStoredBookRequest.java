package com.pageturn.backend.storage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpsertStoredBookRequest(
        @NotBlank
        @Pattern(regexp = "^[a-f0-9]{64}$", message = "bookHash must be a SHA-256 hex string")
        String bookHash,
        @NotBlank
        @Size(max = 255)
        String fileName,
        @Size(max = 120)
        String contentType,
        @Min(0)
        long fileSize,
        @NotBlank
        @Size(max = 1024)
        String storagePath
) {
}
