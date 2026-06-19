package com.pageturn.backend.storage.dto;

public record StoredFile(
        String fileKey,
        String originalFileName,
        String contentType,
        long fileSize,
        boolean publicFile,
        String absolutePath
) {
}
