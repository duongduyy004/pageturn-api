package com.pageturn.backend.auth;

import com.pageturn.backend.auth.dto.AuthResponse;
import com.pageturn.backend.auth.dto.LoginRequest;
import com.pageturn.backend.auth.dto.LogoutRequest;
import com.pageturn.backend.auth.dto.RefreshTokenRequest;
import com.pageturn.backend.auth.dto.RegisterRequest;
import com.pageturn.backend.auth.mapper.AuthMapper;
import com.pageturn.backend.common.exception.ConflictException;
import com.pageturn.backend.common.exception.UnauthorizedException;
import com.pageturn.backend.security.JwtService;
import com.pageturn.backend.user.User;
import com.pageturn.backend.user.UserRepository;
import com.pageturn.backend.user.UserRole;
import com.pageturn.backend.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    public AuthService(UserRepository userRepository,
                       UserService userService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authMapper = authMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        user = userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenService.requireActiveToken(request.refreshToken());
        refreshTokenService.revoke(oldToken);
        return issueTokens(oldToken.getUser());
    }

    @Transactional
    public void logout(Long userId, LogoutRequest request) {
        refreshTokenService.revokeByRawTokenForUser(userId, request.refreshToken());
    }

    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    @Transactional(readOnly = true)
    public AuthResponse.UserSummary getCurrentUser(Long userId) {
        return authMapper.toUserSummary(userService.getEntityById(userId));
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return authMapper.toAuthResponse(accessToken, refreshToken, user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
