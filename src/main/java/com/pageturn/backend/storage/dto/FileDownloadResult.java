package com.pageturn.backend.storage.dto;

import org.springframework.core.io.Resource;

public record FileDownloadResult(
        Resource resource,
        String contentType,
        long contentLength,
        String fileName
) {
}
