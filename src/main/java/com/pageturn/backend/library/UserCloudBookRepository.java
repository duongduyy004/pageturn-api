package com.pageturn.backend.library;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserCloudBookRepository extends JpaRepository<UserCloudBook, Long> {

    List<UserCloudBook> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<UserCloudBook> findByUserIdAndBookHash(Long userId, String bookHash);

    List<UserCloudBook> findAllByUserIdAndUpdatedAtAfter(Long userId, Instant updatedAfter);
}
