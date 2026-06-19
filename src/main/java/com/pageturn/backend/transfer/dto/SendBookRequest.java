package com.pageturn.backend.transfer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendBookRequest(
        @NotBlank @Email String receiverEmail,
        @NotBlank @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash
) {
}
