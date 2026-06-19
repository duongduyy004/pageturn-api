package com.pageturn.backend.security;

import com.pageturn.backend.common.exception.UnauthorizedException;
import com.pageturn.backend.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username.trim().toLowerCase())
                .filter(user -> user.isActive())
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(Long userId) {
        return userRepository.findActiveById(userId)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
    }
}
