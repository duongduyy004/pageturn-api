package com.pageturn.backend.user;

import com.pageturn.backend.common.exception.ConflictException;
import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.user.dto.UpdateProfileRequest;
import com.pageturn.backend.user.dto.UserDto;
import com.pageturn.backend.user.dto.UserSearchDto;
import com.pageturn.backend.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        return userMapper.toDto(getEntityById(userId));
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getEntityById(userId);

        if (StringUtils.hasText(request.email())) {
            String normalizedEmail = normalizeEmail(request.email());
            if (!normalizedEmail.equals(user.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
                throw new ConflictException("Email is already registered");
            }
            user.setEmail(normalizedEmail);
        }

        if (request.displayName() != null) {
            String trimmedDisplayName = request.displayName().trim();
            if (trimmedDisplayName.isEmpty()) {
                throw new IllegalArgumentException("displayName must not be blank");
            }
            user.setDisplayName(trimmedDisplayName);
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserSearchDto> searchByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return List.of();
        }

        return userRepository.findTop10ByActiveTrueAndEmailContainingIgnoreCaseOrderByEmailAsc(email.trim()).stream()
                .map(userMapper::toSearchDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public User getEntityById(Long userId) {
        return userRepository.findActiveById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
