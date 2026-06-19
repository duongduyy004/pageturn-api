package com.pageturn.backend.scheduler;

import com.pageturn.backend.library.UserCloudBook;
import com.pageturn.backend.library.UserCloudBookRepository;
import com.pageturn.backend.storage.StorageProperties;
import com.pageturn.backend.storage.StorageService;
import com.pageturn.backend.storage.entity.StoredBook;
import com.pageturn.backend.storage.repository.StoredBookRepository;
import com.pageturn.backend.store.PublicBook;
import com.pageturn.backend.store.PublicBookRepository;
import com.pageturn.backend.transfer.BookTransfer;
import com.pageturn.backend.transfer.BookTransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class OrphanFileCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(OrphanFileCleanupJob.class);

    private final StorageProperties storageProperties;
    private final StorageService storageService;
    private final UserCloudBookRepository userCloudBookRepository;
    private final PublicBookRepository publicBookRepository;
    private final BookTransferRepository bookTransferRepository;
    private final StoredBookRepository storedBookRepository;
    private final boolean deleteEnabled;
    private final Duration orphanAge;

    public OrphanFileCleanupJob(StorageProperties storageProperties,
                                StorageService storageService,
                                UserCloudBookRepository userCloudBookRepository,
                                PublicBookRepository publicBookRepository,
                                BookTransferRepository bookTransferRepository,
                                StoredBookRepository storedBookRepository,
                                @Value("${app.scheduler.orphan-file-delete-enabled:false}") boolean deleteEnabled,
                                @Value("${app.scheduler.orphan-file-min-age-hours:24}") long orphanAgeHours) {
        this.storageProperties = storageProperties;
        this.storageService = storageService;
        this.userCloudBookRepository = userCloudBookRepository;
        this.publicBookRepository = publicBookRepository;
        this.bookTransferRepository = bookTransferRepository;
        this.storedBookRepository = storedBookRepository;
        this.deleteEnabled = deleteEnabled;
        this.orphanAge = Duration.ofHours(Math.max(orphanAgeHours, 0));
    }

    @Scheduled(cron = "${app.scheduler.orphan-file-cleanup-cron:0 30 2 * * *}")
    @Transactional
    public void run() {
        Instant now = Instant.now();
        log.info("Starting orphan file cleanup job at {}. deleteEnabled={}, minAgeHours={}",
                now, deleteEnabled, orphanAge.toHours());

        Set<String> referencedUploadKeys = collectReferencedUploadKeys();
        Set<String> referencedPublicKeys = collectReferencedPublicKeys();

        int uploadCandidates = scanAndHandleOrphans(storageProperties.uploadDir(), referencedUploadKeys, false, now);
        int publicCandidates = scanAndHandleOrphans(storageProperties.publicDir(), referencedPublicKeys, true, now);

        log.info("Orphan file cleanup job finished. uploadCandidates={}, publicCandidates={}",
                uploadCandidates, publicCandidates);
    }

    private int scanAndHandleOrphans(Path root, Set<String> referencedKeys, boolean publicFile, Instant now) {
        if (!Files.exists(root)) {
            log.info("Skipping orphan scan for {} because the directory does not exist", root);
            return 0;
        }

        int candidates = 0;
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                String fileKey = root.relativize(file).toString().replace('\\', '/');
                if (referencedKeys.contains(fileKey)) {
                    continue;
                }

                candidates++;
                log.info("Orphan file candidate detected: root={} fileKey={}", root, fileKey);
                if (deleteEnabled && isOlderThan(file, now.minus(orphanAge))) {
                    deleteOrphanFile(fileKey, publicFile);
                }
            }
        } catch (IOException ex) {
            log.warn("Failed to scan storage root {} for orphan files: {}", root, ex.getMessage());
        }
        return candidates;
    }

    private void deleteOrphanFile(String fileKey, boolean publicFile) {
        try {
            if (publicFile) {
                storageService.deletePublicFile(fileKey);
            } else {
                storageService.deleteUploadFile(fileKey);
            }
            log.info("Deleted orphan file: fileKey={} publicFile={}", fileKey, publicFile);
        } catch (RuntimeException ex) {
            log.warn("Failed to delete orphan file fileKey={} publicFile={}: {}", fileKey, publicFile, ex.getMessage());
        }
    }

    private boolean isOlderThan(Path file, Instant cutoff) {
        try {
            return Files.getLastModifiedTime(file).toInstant().isBefore(cutoff);
        } catch (IOException ex) {
            log.warn("Failed to read lastModifiedTime for {}: {}", file, ex.getMessage());
            return false;
        }
    }

    private Set<String> collectReferencedUploadKeys() {
        Set<String> keys = new HashSet<>();
        for (UserCloudBook book : userCloudBookRepository.findAll()) {
            addKey(keys, book.getFileKey());
            addKey(keys, book.getCoverKey());
        }
        for (BookTransfer transfer : bookTransferRepository.findAll()) {
            addKey(keys, transfer.getFileKey());
        }
        for (StoredBook storedBook : storedBookRepository.findAll()) {
            addNormalizedAbsolutePath(keys, storageProperties.uploadDir(), storedBook.getStoragePath());
            addNormalizedAbsolutePath(keys, storageProperties.publicDir(), storedBook.getStoragePath());
        }
        return keys;
    }

    private Set<String> collectReferencedPublicKeys() {
        Set<String> keys = new HashSet<>();
        for (PublicBook book : publicBookRepository.findAll()) {
            addKey(keys, book.getFileKey());
            addKey(keys, book.getCoverKey());
        }
        for (StoredBook storedBook : storedBookRepository.findAll()) {
            addNormalizedAbsolutePath(keys, storageProperties.publicDir(), storedBook.getStoragePath());
        }
        return keys;
    }

    private void addKey(Set<String> target, String key) {
        if (StringUtils.hasText(key)) {
            target.add(key.trim());
        }
    }

    private void addNormalizedAbsolutePath(Set<String> target, Path root, String storedPath) {
        if (!StringUtils.hasText(storedPath)) {
            return;
        }
        try {
            Path normalized = Path.of(storedPath).toAbsolutePath().normalize();
            if (normalized.startsWith(root)) {
                target.add(root.relativize(normalized).toString().replace('\\', '/'));
            }
        } catch (RuntimeException ex) {
            log.debug("Skipping non-path stored reference '{}': {}", storedPath, ex.getMessage());
        }
    }
}
