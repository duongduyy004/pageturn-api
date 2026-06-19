package com.pageturn.backend.highlight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    List<Highlight> findAllByUserIdAndBookHashAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, String bookHash);

    List<Highlight> findAllByUserIdAndUpdatedAtAfterOrderByUpdatedAtAsc(Long userId, Instant updatedAfter);

    Optional<Highlight> findByIdAndUserId(Long id, Long userId);
}
