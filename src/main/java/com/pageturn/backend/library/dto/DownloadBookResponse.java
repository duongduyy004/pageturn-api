package com.pageturn.backend.library.dto;

import org.springframework.core.io.Resource;

public record DownloadBookResponse(
        String bookHash,
        String fileName,
        String contentType,
        long contentLength,
        Resource resource
) {
}
