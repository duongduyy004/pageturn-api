package com.pageturn.backend.user.mapper;

import com.pageturn.backend.user.User;
import com.pageturn.backend.user.dto.UserDto;
import com.pageturn.backend.user.dto.UserSearchDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UserSearchDto toSearchDto(User user) {
        return new UserSearchDto(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
