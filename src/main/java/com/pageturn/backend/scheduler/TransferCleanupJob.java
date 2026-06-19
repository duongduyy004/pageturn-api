package com.pageturn.backend.scheduler;

import com.pageturn.backend.storage.StorageService;
import com.pageturn.backend.transfer.BookTransfer;
import com.pageturn.backend.transfer.BookTransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class TransferCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TransferCleanupJob.class);
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_EXPIRED = "expired";

    private final BookTransferRepository bookTransferRepository;
    private final StorageService storageService;
    private final Duration expiredFileRetention;

    public TransferCleanupJob(BookTransferRepository bookTransferRepository,
                              StorageService storageService,
                              @Value("${app.scheduler.transfer-file-retention-days:1}") long transferFileRetentionDays) {
        this.bookTransferRepository = bookTransferRepository;
        this.storageService = storageService;
        this.expiredFileRetention = Duration.ofDays(Math.max(transferFileRetentionDays, 0));
    }

    @Scheduled(cron = "${app.scheduler.transfer-cleanup-cron:0 0 2 * * *}")
    @Transactional
    public void run() {
        Instant now = Instant.now();
        log.info("Starting transfer cleanup job at {}", now);

        List<BookTransfer> transfersToExpire = bookTransferRepository.findAllByStatusAndExpiresAtBefore(STATUS_PENDING, now);
        transfersToExpire.forEach(transfer -> transfer.setStatus(STATUS_EXPIRED));
        if (!transfersToExpire.isEmpty()) {
            bookTransferRepository.saveAll(transfersToExpire);
        }
        log.info("Transfer cleanup job expired {} pending transfers", transfersToExpire.size());

        deleteExpiredTransferFiles(now.minus(expiredFileRetention));
        log.info("Transfer cleanup job finished");
    }

    private void deleteExpiredTransferFiles(Instant deleteBefore) {
        List<BookTransfer> expiredTransfers = bookTransferRepository
                .findAllByStatusAndExpiresAtBeforeAndFileKeyIsNotNull(STATUS_EXPIRED, deleteBefore);

        int deleted = 0;
        for (BookTransfer transfer : expiredTransfers) {
            if (!StringUtils.hasText(transfer.getFileKey())) {
                continue;
            }

            try {
                storageService.deleteUploadFile(transfer.getFileKey());
                transfer.setFileKey(null);
                bookTransferRepository.save(transfer);
                deleted++;
            } catch (RuntimeException ex) {
                log.warn("Failed to delete expired transfer file for transferId={} fileKey={}: {}",
                        transfer.getId(), transfer.getFileKey(), ex.getMessage());
            }
        }

        log.info("Transfer cleanup job deleted {} expired transfer files older than {}", deleted, deleteBefore);
    }
}
