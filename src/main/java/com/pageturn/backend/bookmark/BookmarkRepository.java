package com.pageturn.backend.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findAllByUserIdAndBookHashAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, String bookHash);

    List<Bookmark> findAllByUserIdAndUpdatedAtAfterOrderByUpdatedAtAsc(Long userId, Instant updatedAfter);

    Optional<Bookmark> findByIdAndUserId(Long id, Long userId);
}
