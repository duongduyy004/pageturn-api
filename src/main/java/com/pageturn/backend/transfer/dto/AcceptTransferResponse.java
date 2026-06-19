package com.pageturn.backend.transfer.dto;

public record AcceptTransferResponse(
        Long transferId,
        String status,
        String originalHash,
        String bookTitle,
        String fileKey,
        String fileName,
        String contentType,
        long fileSize,
        boolean duplicateBook
) {
}
