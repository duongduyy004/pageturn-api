package com.pageturn.backend.sync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record SyncCollectionDto(
        Long id,
        @Size(max = 120)
        String name,
        @Size(max = 2000)
        String description,
        List<@Valid SyncCollectionBookItem> books,
        Instant updatedAt
) {
    public record SyncCollectionBookItem(
            @Pattern(regexp = "^[a-f0-9]{64}$")
            String bookHash,
            @Min(0)
            int position
    ) {
    }
}
