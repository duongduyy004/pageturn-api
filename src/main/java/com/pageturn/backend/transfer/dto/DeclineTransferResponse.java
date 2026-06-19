package com.pageturn.backend.transfer.dto;

public record DeclineTransferResponse(
        Long transferId,
        String status
) {
}
