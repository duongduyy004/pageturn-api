package com.pageturn.backend.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {

    Optional<ReadingProgress> findByUserIdAndBookHash(Long userId, String bookHash);

    List<ReadingProgress> findAllByUserIdAndUpdatedAtAfter(Long userId, Instant updatedAfter);
}
