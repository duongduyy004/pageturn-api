package com.pageturn.backend.store.dto;

public record PublicBookDto(
        Long id,
        String bookHash,
        String title,
        String author,
        String language,
        String coverKey,
        String fileFormat,
        long fileSize,
        String category,
        long downloadCount
) {
}
