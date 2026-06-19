package com.pageturn.backend.auth;

import com.pageturn.backend.common.exception.UnauthorizedException;
import com.pageturn.backend.common.util.HashUtils;
import com.pageturn.backend.config.AppConfig;
import com.pageturn.backend.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppConfig appConfig;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, AppConfig appConfig) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.appConfig = appConfig;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawRefreshToken = generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setRefreshTokenHash(HashUtils.sha256(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(appConfig.jwt().refreshTokenTtlSeconds()));
        refreshToken.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return rawRefreshToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken requireActiveToken(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshTokenHash(HashUtils.sha256(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid"));
        if (!refreshToken.isActive()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }
        return refreshToken;
    }

    @Transactional
    public void revoke(RefreshToken refreshToken) {
        refreshToken.setRevokedAt(Instant.now());
        refreshToken.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeByRawToken(String rawRefreshToken) {
        refreshTokenRepository.findByRefreshTokenHash(HashUtils.sha256(rawRefreshToken))
                .ifPresent(this::revoke);
    }

    @Transactional
    public void revokeByRawTokenForUser(Long userId, String rawRefreshToken) {
        refreshTokenRepository.findByRefreshTokenHash(HashUtils.sha256(rawRefreshToken))
                .filter(token -> token.getUser().getId().equals(userId))
                .ifPresent(this::revoke);
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        Instant now = Instant.now();
        refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId).forEach(token -> {
            token.setRevokedAt(now);
            token.setLastUsedAt(now);
        });
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
