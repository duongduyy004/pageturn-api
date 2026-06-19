package com.pageturn.backend.collection.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddBookToCollectionRequest(
        @NotBlank
        @Pattern(regexp = "^[a-f0-9]{64}$")
        String bookHash,
        @Min(0)
        Integer position
) {
}
