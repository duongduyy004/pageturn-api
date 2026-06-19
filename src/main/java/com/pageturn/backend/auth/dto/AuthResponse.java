package com.pageturn.backend.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserSummary user
) {
    public record UserSummary(
            Long id,
            String email,
            String displayName,
            String role
    ) {
    }
}
