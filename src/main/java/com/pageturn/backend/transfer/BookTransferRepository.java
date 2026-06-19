package com.pageturn.backend.transfer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookTransferRepository extends JpaRepository<BookTransfer, Long> {

    List<BookTransfer> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    Optional<BookTransfer> findByIdAndReceiverId(Long id, Long receiverId);

    List<BookTransfer> findAllByStatusAndExpiresAtBefore(String status, Instant expiresAt);

    List<BookTransfer> findAllByStatusAndExpiresAtBeforeAndFileKeyIsNotNull(String status, Instant expiresAt);
}
