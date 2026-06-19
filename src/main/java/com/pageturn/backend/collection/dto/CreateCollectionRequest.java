package com.pageturn.backend.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCollectionRequest(
        @NotBlank
        @Size(max = 120)
        String name,
        @Size(max = 2000)
        String description
) {
}
