package com.pageturn.backend.storage.service;

import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.storage.dto.StoredBookResponse;
import com.pageturn.backend.storage.dto.UpsertStoredBookRequest;
import com.pageturn.backend.storage.entity.StoredBook;
import com.pageturn.backend.storage.repository.StoredBookRepository;
import com.pageturn.backend.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class StorageService {

    private final StoredBookRepository storedBookRepository;
    private final UserService userService;

    public StorageService(StoredBookRepository storedBookRepository, UserService userService) {
        this.storedBookRepository = storedBookRepository;
        this.userService = userService;
    }

    @Transactional
    public StoredBookResponse upsert(Long userId, UpsertStoredBookRequest request) {
        StoredBook book = storedBookRepository.findByUserIdAndBookHash(userId, request.bookHash())
                .orElseGet(StoredBook::new);
        if (book.getId() == null) {
            book.setUser(userService.getEntityById(userId));
            book.setBookHash(request.bookHash());
        }
        book.setFileName(request.fileName().trim());
        book.setContentType(request.contentType());
        book.setFileSize(request.fileSize());
        book.setStoragePath(request.storagePath().trim());
        return toResponse(storedBookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public List<StoredBookResponse> list(Long userId) {
        return storedBookRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<StoredBookResponse> listUpdatedAfter(Long userId, Instant updatedAfter) {
        return storedBookRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).stream()
                .filter(book -> book.getUpdatedAt().isAfter(updatedAfter))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoredBookResponse get(Long userId, String bookHash) {
        return toResponse(getEntity(userId, bookHash));
    }

    @Transactional
    public void delete(Long userId, String bookHash) {
        storedBookRepository.delete(getEntity(userId, bookHash));
    }

    @Transactional(readOnly = true)
    public StoredBook getEntity(Long userId, String bookHash) {
        return storedBookRepository.findByUserIdAndBookHash(userId, bookHash)
                .orElseThrow(() -> new NotFoundException("Stored book not found"));
    }

    private StoredBookResponse toResponse(StoredBook book) {
        return new StoredBookResponse(
                book.getBookHash(),
                book.getFileName(),
                book.getContentType(),
                book.getFileSize(),
                book.getStoragePath(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
