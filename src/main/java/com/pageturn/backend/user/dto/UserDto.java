package com.pageturn.backend.user.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String email,
        String displayName,
        String role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
