package com.pageturn.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email
        @NotBlank
        @Size(max = 320)
        String email,
        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*\\d).{8,72}$", message = "password must be at least 8 characters and contain at least 1 number")
        String password,
        @NotBlank
        @Size(max = 120)
        String displayName
) {
}
