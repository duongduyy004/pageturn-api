package com.pageturn.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshTokenHash(String refreshTokenHash);

    List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(Long userId);

    long deleteByExpiresAtBeforeOrRevokedAtBefore(Instant expiresBefore, Instant revokedBefore);
}
