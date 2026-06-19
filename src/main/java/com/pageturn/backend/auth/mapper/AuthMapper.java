package com.pageturn.backend.auth.mapper;

import com.pageturn.backend.auth.dto.AuthResponse;
import com.pageturn.backend.user.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(String accessToken, String refreshToken, User user) {
        return new AuthResponse(accessToken, refreshToken, toUserSummary(user));
    }

    public AuthResponse.UserSummary toUserSummary(User user) {
        return new AuthResponse.UserSummary(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole().name()
        );
    }
}
