package com.pageturn.backend.transfer;

import com.pageturn.backend.common.exception.BadRequestException;
import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.library.LibraryService;
import com.pageturn.backend.library.UserCloudBook;
import com.pageturn.backend.library.UserCloudBookRepository;
import com.pageturn.backend.storage.StorageService;
import com.pageturn.backend.storage.dto.FileDownloadResult;
import com.pageturn.backend.storage.dto.StoredFile;
import com.pageturn.backend.transfer.dto.AcceptTransferResponse;
import com.pageturn.backend.transfer.dto.DeclineTransferResponse;
import com.pageturn.backend.transfer.dto.SendBookRequest;
import com.pageturn.backend.transfer.dto.TransferDto;
import com.pageturn.backend.user.User;
import com.pageturn.backend.user.UserRepository;
import com.pageturn.backend.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class BookTransferService {

    private static final long TRANSFER_TTL_SECONDS = 7L * 24L * 60L * 60L;
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACCEPTED = "accepted";
    private static final String STATUS_DECLINED = "declined";
    private static final String STATUS_EXPIRED = "expired";

    private final BookTransferRepository bookTransferRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final LibraryService libraryService;
    private final UserCloudBookRepository userCloudBookRepository;
    private final StorageService storageService;

    public BookTransferService(BookTransferRepository bookTransferRepository,
                               UserRepository userRepository,
                               UserService userService,
                               LibraryService libraryService,
                               UserCloudBookRepository userCloudBookRepository,
                               StorageService storageService) {
        this.bookTransferRepository = bookTransferRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.libraryService = libraryService;
        this.userCloudBookRepository = userCloudBookRepository;
        this.storageService = storageService;
    }

    @Transactional
    public TransferDto send(Long senderId, SendBookRequest request) {
        User sender = userService.getEntityById(senderId);
        User receiver = getActiveReceiver(request.receiverEmail());
        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("Sender cannot transfer a book to the same account");
        }

        UserCloudBook sourceBook = libraryService.getEntity(senderId, request.bookHash());
        if (!StringUtils.hasText(sourceBook.getFileKey())) {
            throw new BadRequestException("Cloud book file is required before sending a transfer");
        }

        BookTransfer transfer = new BookTransfer();
        transfer.setSender(sender);
        transfer.setReceiver(receiver);
        transfer.setBookTitle(sourceBook.getTitle());
        transfer.setOriginalHash(sourceBook.getBookHash());
        transfer.setStatus(STATUS_PENDING);
        transfer.setExpiresAt(Instant.now().plusSeconds(TRANSFER_TTL_SECONDS));
        transfer = bookTransferRepository.saveAndFlush(transfer);

        StoredFile storedFile = storageService.copyUploadFileToTransfer(
                transfer.getId(),
                sourceBook.getFileKey(),
                sourceBook.getBookHash()
        );
        transfer.setFileKey(storedFile.fileKey());

        return toDto(bookTransferRepository.save(transfer));
    }

    @Transactional
    public List<TransferDto> getInbox(Long receiverId) {
        expirePendingTransfers();
        return bookTransferRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AcceptTransferResponse accept(Long receiverId, Long transferId) {
        BookTransfer transfer = getReceiverTransfer(receiverId, transferId);
        ensurePendingAndNotExpired(transfer);
        transfer.setStatus(STATUS_ACCEPTED);
        bookTransferRepository.save(transfer);

        FileDownloadResult download = storageService.downloadUploadFile(transfer.getFileKey());
        return new AcceptTransferResponse(
                transfer.getId(),
                transfer.getStatus(),
                transfer.getOriginalHash(),
                transfer.getBookTitle(),
                transfer.getFileKey(),
                download.fileName(),
                normalizeContentType(download.contentType()),
                download.contentLength(),
                hasDuplicateBook(transfer.getReceiver().getId(), transfer.getOriginalHash())
        );
    }

    @Transactional
    public DeclineTransferResponse decline(Long receiverId, Long transferId) {
        BookTransfer transfer = getReceiverTransfer(receiverId, transferId);
        ensurePendingAndNotExpired(transfer);
        transfer.setStatus(STATUS_DECLINED);
        bookTransferRepository.save(transfer);
        return new DeclineTransferResponse(
                transfer.getId(),
                transfer.getStatus()
        );
    }

    @Transactional
    public int expirePendingTransfers() {
        List<BookTransfer> expiredTransfers = bookTransferRepository.findAllByStatusAndExpiresAtBefore(
                STATUS_PENDING,
                Instant.now()
        );
        expiredTransfers.forEach(transfer -> transfer.setStatus(STATUS_EXPIRED));
        bookTransferRepository.saveAll(expiredTransfers);
        return expiredTransfers.size();
    }

    private User getActiveReceiver(String receiverEmail) {
        String normalizedEmail = receiverEmail.trim().toLowerCase(Locale.ROOT);
        User receiver = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("Receiver not found"));
        if (!receiver.isActive()) {
            throw new NotFoundException("Receiver not found");
        }
        return receiver;
    }

    private BookTransfer getReceiverTransfer(Long receiverId, Long transferId) {
        return bookTransferRepository.findByIdAndReceiverId(transferId, receiverId)
                .orElseThrow(() -> new NotFoundException("Transfer not found"));
    }

    private void ensurePendingAndNotExpired(BookTransfer transfer) {
        if (!STATUS_PENDING.equals(transfer.getStatus())) {
            throw new BadRequestException("Transfer is no longer pending");
        }
        if (transfer.getExpiresAt().isBefore(Instant.now())) {
            transfer.setStatus(STATUS_EXPIRED);
            bookTransferRepository.save(transfer);
            throw new BadRequestException("Transfer has expired");
        }
    }

    private TransferDto toDto(BookTransfer transfer) {
        return new TransferDto(
                transfer.getId(),
                transfer.getSender().getId(),
                transfer.getSender().getEmail(),
                transfer.getSender().getDisplayName(),
                transfer.getReceiver().getId(),
                transfer.getReceiver().getEmail(),
                transfer.getFileKey(),
                transfer.getBookTitle(),
                transfer.getOriginalHash(),
                transfer.getStatus(),
                hasDuplicateBook(transfer.getReceiver().getId(), transfer.getOriginalHash()),
                transfer.getCreatedAt(),
                transfer.getExpiresAt()
        );
    }

    private boolean hasDuplicateBook(Long receiverId, String originalHash) {
        return userCloudBookRepository.findByUserIdAndBookHash(receiverId, originalHash).isPresent();
    }

    private String normalizeContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
    }
}
