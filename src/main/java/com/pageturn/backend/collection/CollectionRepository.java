package com.pageturn.backend.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<Collection> findByIdAndUserId(Long id, Long userId);

    List<Collection> findAllByUserIdAndUpdatedAtAfter(Long userId, Instant updatedAfter);
}
