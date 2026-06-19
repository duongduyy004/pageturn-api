package com.pageturn.backend.progress;

import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.library.LibraryService;
import com.pageturn.backend.progress.dto.ProgressDto;
import com.pageturn.backend.progress.dto.SaveProgressRequest;
import com.pageturn.backend.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;
    private final UserService userService;
    private final LibraryService libraryService;

    public ReadingProgressService(ReadingProgressRepository readingProgressRepository,
                                  UserService userService,
                                  LibraryService libraryService) {
        this.readingProgressRepository = readingProgressRepository;
        this.userService = userService;
        this.libraryService = libraryService;
    }

    @Transactional
    public ProgressDto save(Long userId, SaveProgressRequest request) {
        ReadingProgress progress = readingProgressRepository.findByUserIdAndBookHash(userId, request.bookHash())
                .orElseGet(ReadingProgress::new);
        if (progress.getId() == null) {
            progress.setUser(userService.getEntityById(userId));
            progress.setBookHash(request.bookHash());
        }
        progress.setChapterIdx(request.chapterIdx());
        progress.setScrollPct(request.scrollPct());
        libraryService.touchBook(userId, request.bookHash());
        return toDto(readingProgressRepository.save(progress));
    }

    @Transactional(readOnly = true)
    public ProgressDto get(Long userId, String bookHash) {
        return toDto(getEntity(userId, bookHash));
    }

    @Transactional(readOnly = true)
    public List<ProgressDto> listUpdatedAfter(Long userId, Instant updatedAfter) {
        return readingProgressRepository.findAllByUserIdAndUpdatedAtAfter(userId, updatedAfter).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReadingProgress getEntity(Long userId, String bookHash) {
        return readingProgressRepository.findByUserIdAndBookHash(userId, bookHash)
                .orElseThrow(() -> new NotFoundException("Reading progress not found"));
    }

    private ProgressDto toDto(ReadingProgress progress) {
        return new ProgressDto(
                progress.getBookHash(),
                progress.getChapterIdx(),
                progress.getScrollPct(),
                progress.getUpdatedAt()
        );
    }
}
