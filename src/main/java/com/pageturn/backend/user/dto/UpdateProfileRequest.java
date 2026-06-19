package com.pageturn.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Email
        @Size(max = 320)
        String email,
        @Size(max = 120)
        String displayName
) {
}
