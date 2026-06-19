package com.pageturn.backend.collection.dto;

import java.time.Instant;
import java.util.List;

public record CollectionDto(
        Long id,
        String name,
        String description,
        List<CollectionBookItem> books,
        Instant createdAt,
        Instant updatedAt
) {
    public record CollectionBookItem(
            String bookHash,
            int position
    ) {
    }
}
