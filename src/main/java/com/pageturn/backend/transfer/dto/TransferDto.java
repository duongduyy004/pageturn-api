package com.pageturn.backend.transfer.dto;

import java.time.Instant;

public record TransferDto(
        Long id,
        Long senderId,
        String senderEmail,
        String senderDisplayName,
        Long receiverId,
        String receiverEmail,
        String fileKey,
        String bookTitle,
        String originalHash,
        String status,
        boolean duplicateBook,
        Instant createdAt,
        Instant expiresAt
) {
}
