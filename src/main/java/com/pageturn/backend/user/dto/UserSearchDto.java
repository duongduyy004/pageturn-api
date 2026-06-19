package com.pageturn.backend.user.dto;

public record UserSearchDto(
        Long id,
        String email,
        String displayName
) {
}
