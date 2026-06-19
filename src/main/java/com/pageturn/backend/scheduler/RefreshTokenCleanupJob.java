package com.pageturn.backend.scheduler;

import com.pageturn.backend.auth.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupJob(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "${app.scheduler.refresh-token-cleanup-cron:0 15 2 * * *}")
    @Transactional
    public void run() {
        Instant now = Instant.now();
        Instant revokedCutoff = now.minusSeconds(86400);
        log.info("Starting refresh token cleanup job at {}", now);
        long deleted = refreshTokenRepository.deleteByExpiresAtBeforeOrRevokedAtBefore(now, revokedCutoff);
        log.info("Refresh token cleanup job finished. Deleted {} expired or revoked refresh tokens", deleted);
    }
}
