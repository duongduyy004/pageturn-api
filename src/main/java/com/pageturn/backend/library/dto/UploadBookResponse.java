package com.pageturn.backend.library.dto;

public record UploadBookResponse(
        String bookHash,
        String fileKey,
        String fileFormat,
        long fileSize,
        boolean cloudSynced
) {
}
