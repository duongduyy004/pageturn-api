package com.pageturn.backend.store;

import com.pageturn.backend.common.exception.BadRequestException;
import com.pageturn.backend.common.exception.ConflictException;
import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.storage.StorageService;
import com.pageturn.backend.storage.dto.FileDownloadResult;
import com.pageturn.backend.storage.dto.StoredFile;
import com.pageturn.backend.store.dto.CreatePublicBookRequest;
import com.pageturn.backend.store.dto.PublicBookDetailDto;
import com.pageturn.backend.store.dto.PublicBookDto;
import com.pageturn.backend.store.dto.UpdatePublicBookRequest;
import com.pageturn.backend.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class PublicBookService {

    private final PublicBookRepository publicBookRepository;
    private final UserService userService;
    private final StorageService storageService;

    public PublicBookService(PublicBookRepository publicBookRepository,
                             UserService userService,
                             StorageService storageService) {
        this.publicBookRepository = publicBookRepository;
        this.userService = userService;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public Page<PublicBookDto> listActive(String category, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizePageSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
        return publicBookRepository.searchActive(normalizeFilter(category), normalizeFilter(keyword), pageable)
                .map(this::toPublicBookDto);
    }

    @Transactional(readOnly = true)
    public PublicBookDetailDto getActive(Long id) {
        return toDetailDto(publicBookRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Public book not found")));
    }

    @Transactional
    public PublicBookDetailDto create(Long adminUserId, CreatePublicBookRequest request) {
        String normalizedBookHash = request.getBookHash().trim().toLowerCase(Locale.ROOT);
        if (publicBookRepository.existsByBookHash(normalizedBookHash)) {
            throw new ConflictException("Public book hash already exists");
        }

        StoredFile storedFile = storageService.savePublicBook(normalizedBookHash, request.getFile());
        StoredFile storedCover = hasFile(request.getCoverImage())
                ? storageService.savePublicCoverImage(normalizedBookHash, request.getCoverImage())
                : null;

        PublicBook book = new PublicBook();
        book.setBookHash(normalizedBookHash);
        book.setAddedBy(userService.getEntityById(adminUserId));
        applyMetadata(book, request.getTitle(), request.getAuthor(), request.getDescription(), request.getLanguage(),
                request.getCategory(), request.getActive());
        book.setFileKey(storedFile.fileKey());
        book.setFileFormat(extractExtension(storedFile.fileKey()));
        book.setFileSize(storedFile.fileSize());
        if (storedCover != null) {
            book.setCoverKey(storedCover.fileKey());
        }
        return toDetailDto(publicBookRepository.save(book));
    }

    @Transactional
    public PublicBookDetailDto update(Long id, UpdatePublicBookRequest request) {
        PublicBook book = publicBookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Public book not found"));

        String previousFileKey = book.getFileKey();
        String previousCoverKey = book.getCoverKey();

        if (StringUtils.hasText(request.getBookHash())) {
            String normalizedBookHash = request.getBookHash().trim().toLowerCase(Locale.ROOT);
            if (!normalizedBookHash.equals(book.getBookHash())) {
                throw new BadRequestException("Updating bookHash is not supported for existing public books");
            }
        }

        applyMetadata(book, request.getTitle(), request.getAuthor(), request.getDescription(), request.getLanguage(),
                request.getCategory(), request.getActive());

        if (hasFile(request.getFile())) {
            StoredFile storedFile = storageService.savePublicBook(book.getBookHash(), request.getFile());
            book.setFileKey(storedFile.fileKey());
            book.setFileFormat(extractExtension(storedFile.fileKey()));
            book.setFileSize(storedFile.fileSize());
        }

        if (hasFile(request.getCoverImage())) {
            StoredFile storedCover = storageService.savePublicCoverImage(book.getBookHash(), request.getCoverImage());
            book.setCoverKey(storedCover.fileKey());
        }

        PublicBook saved = publicBookRepository.save(book);
        deleteOldPublicAsset(previousFileKey, saved.getFileKey());
        deleteOldPublicAsset(previousCoverKey, saved.getCoverKey());
        return toDetailDto(saved);
    }

    @Transactional
    public FileDownloadResult download(Long id) {
        PublicBook book = publicBookRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Public book not found"));
        book.setDownloadCount(book.getDownloadCount() + 1);
        publicBookRepository.save(book);
        return storageService.downloadPublicFile(book.getFileKey());
    }

    private void applyMetadata(PublicBook book,
                               String title,
                               String author,
                               String description,
                               String language,
                               String category,
                               Boolean active) {
        if (!StringUtils.hasText(title) && book.getId() == null) {
            throw new BadRequestException("Title is required");
        }
        if (title != null) {
            String normalizedTitle = title.trim();
            if (normalizedTitle.isEmpty()) {
                throw new BadRequestException("Title must not be blank");
            }
            book.setTitle(normalizedTitle);
        }
        if (author != null) {
            book.setAuthor(trimToNull(author));
        }
        if (description != null) {
            book.setDescription(trimToNull(description));
        }
        if (language != null) {
            book.setLanguage(trimToNull(language));
        }
        if (category != null) {
            book.setCategory(trimToNull(category));
        }
        if (active != null) {
            book.setActive(active);
        } else if (book.getId() == null) {
            book.setActive(true);
        }
    }

    private PublicBookDto toPublicBookDto(PublicBook book) {
        return new PublicBookDto(
                book.getId(),
                book.getBookHash(),
                book.getTitle(),
                book.getAuthor(),
                book.getLanguage(),
                book.getCoverKey(),
                book.getFileFormat(),
                book.getFileSize(),
                book.getCategory(),
                book.getDownloadCount()
        );
    }

    private PublicBookDetailDto toDetailDto(PublicBook book) {
        return new PublicBookDetailDto(
                book.getId(),
                book.getBookHash(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getLanguage(),
                book.getCoverKey(),
                book.getFileKey(),
                book.getFileFormat(),
                book.getFileSize(),
                book.getCategory(),
                book.getAddedBy().getId(),
                book.getDownloadCount(),
                book.isActive(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 100);
    }

    private String normalizeFilter(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean hasFile(org.springframework.web.multipart.MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String extractExtension(String fileKey) {
        int dotIndex = fileKey.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == fileKey.length() - 1) {
            throw new BadRequestException("Stored file must have a valid extension");
        }
        return fileKey.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void deleteOldPublicAsset(String previousFileKey, String currentFileKey) {
        if (StringUtils.hasText(previousFileKey) && !previousFileKey.equals(currentFileKey)) {
            storageService.deletePublicFile(previousFileKey);
        }
    }
}
