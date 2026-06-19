package com.pageturn.backend.storage.repository;

import com.pageturn.backend.storage.entity.StoredBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoredBookRepository extends JpaRepository<StoredBook, Long> {

    List<StoredBook> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<StoredBook> findByUserIdAndBookHash(Long userId, String bookHash);
}
