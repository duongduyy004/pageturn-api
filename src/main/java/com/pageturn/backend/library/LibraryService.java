package com.pageturn.backend.library;

import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.library.dto.CloudBookDto;
import com.pageturn.backend.library.dto.DownloadBookResponse;
import com.pageturn.backend.library.dto.UpsertBookRequest;
import com.pageturn.backend.library.dto.UploadBookResponse;
import com.pageturn.backend.library.mapper.LibraryMapper;
import com.pageturn.backend.storage.StorageService;
import com.pageturn.backend.storage.dto.FileDownloadResult;
import com.pageturn.backend.storage.dto.StoredFile;
import com.pageturn.backend.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
public class LibraryService {

    private final UserCloudBookRepository userCloudBookRepository;
    private final UserService userService;
    private final StorageService storageService;
    private final LibraryMapper libraryMapper;

    public LibraryService(UserCloudBookRepository userCloudBookRepository,
                          UserService userService,
                          StorageService storageService,
                          LibraryMapper libraryMapper) {
        this.userCloudBookRepository = userCloudBookRepository;
        this.userService = userService;
        this.storageService = storageService;
        this.libraryMapper = libraryMapper;
    }

    @Transactional
    public CloudBookDto upsert(Long userId, UpsertBookRequest request) {
        UserCloudBook book = userCloudBookRepository.findByUserIdAndBookHash(userId, request.bookHash())
                .orElseGet(UserCloudBook::new);
        if (book.getId() == null) {
            book.setUser(userService.getEntityById(userId));
            book.setBookHash(request.bookHash());
            book.setAddedAt(Instant.now());
        }
        book.setTitle(request.title().trim());
        book.setAuthor(trimToNull(request.author()));
        if (StringUtils.hasText(request.fileFormat())) {
            book.setFileFormat(request.fileFormat().trim().toLowerCase());
        }
        return libraryMapper.toCloudBookDto(userCloudBookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public List<CloudBookDto> list(Long userId) {
        return userCloudBookRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(libraryMapper::toCloudBookDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CloudBookDto> listUpdatedAfter(Long userId, Instant updatedAfter) {
        return userCloudBookRepository.findAllByUserIdAndUpdatedAtAfter(userId, updatedAfter).stream()
                .map(libraryMapper::toCloudBookDto)
                .toList();
    }

    @Transactional
    public UploadBookResponse upload(Long userId, String bookHash, MultipartFile file) {
        UserCloudBook book = getEntity(userId, bookHash);
        StoredFile storedFile = storageService.saveUserBook(userId, bookHash, file);
        book.setFileKey(storedFile.fileKey());
        book.setFileSize(storedFile.fileSize());
        book.setFileFormat(extractExtension(storedFile.fileKey()));
        book.setCloudSynced(true);
        userCloudBookRepository.save(book);
        return libraryMapper.toUploadBookResponse(book, storedFile);
    }

    @Transactional(readOnly = true)
    public DownloadBookResponse download(Long userId, String bookHash) {
        UserCloudBook book = getEntity(userId, bookHash);
        if (!StringUtils.hasText(book.getFileKey())) {
            throw new NotFoundException("Book file not found in cloud storage");
        }
        FileDownloadResult result = storageService.downloadUploadFile(book.getFileKey());
        return libraryMapper.toDownloadBookResponse(book, result);
    }

    @Transactional
    public void delete(Long userId, String bookHash, boolean deletePhysicalFile) {
        UserCloudBook book = getEntity(userId, bookHash);
        if (deletePhysicalFile) {
            deleteIfPresent(book.getFileKey());
            deleteIfPresent(book.getCoverKey());
        }
        userCloudBookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public UserCloudBook getEntity(Long userId, String bookHash) {
        return userCloudBookRepository.findByUserIdAndBookHash(userId, bookHash)
                .orElseThrow(() -> new NotFoundException("Cloud book not found"));
    }

    @Transactional
    public void touchBook(Long userId, String bookHash) {
        UserCloudBook book = getEntity(userId, bookHash);
        userCloudBookRepository.save(book);
    }

    private void deleteIfPresent(String fileKey) {
        if (StringUtils.hasText(fileKey)) {
            storageService.deleteUploadFile(fileKey);
        }
    }

    private String extractExtension(String fileKey) {
        int dotIndex = fileKey.lastIndexOf('.');
        return dotIndex > 0 ? fileKey.substring(dotIndex + 1).toLowerCase() : null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
