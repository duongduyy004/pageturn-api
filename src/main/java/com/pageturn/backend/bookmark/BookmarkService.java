package com.pageturn.backend.bookmark;

import com.pageturn.backend.bookmark.dto.BookmarkDto;
import com.pageturn.backend.bookmark.dto.CreateBookmarkRequest;
import com.pageturn.backend.bookmark.dto.UpdateBookmarkRequest;
import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserService userService;

    public BookmarkService(BookmarkRepository bookmarkRepository, UserService userService) {
        this.bookmarkRepository = bookmarkRepository;
        this.userService = userService;
    }

    @Transactional
    public BookmarkDto create(Long userId, CreateBookmarkRequest request) {
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(userService.getEntityById(userId));
        bookmark.setBookHash(request.bookHash());
        bookmark.setChapterIdx(request.chapterIdx());
        bookmark.setScrollPct(request.scrollPct());
        bookmark.setSnippet(request.snippet());
        bookmark.setDeleted(false);
        return toDto(bookmarkRepository.save(bookmark));
    }

    @Transactional(readOnly = true)
    public List<BookmarkDto> listByBook(Long userId, String bookHash) {
        return bookmarkRepository.findAllByUserIdAndBookHashAndIsDeletedFalseOrderByCreatedAtDesc(userId, bookHash).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookmarkDto> listUpdatedAfter(Long userId, Instant updatedAfter) {
        return bookmarkRepository.findAllByUserIdAndUpdatedAtAfterOrderByUpdatedAtAsc(userId, updatedAfter).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public BookmarkDto update(Long userId, Long bookmarkId, UpdateBookmarkRequest request) {
        Bookmark bookmark = getEntity(userId, bookmarkId);
        bookmark.setChapterIdx(request.chapterIdx());
        bookmark.setScrollPct(request.scrollPct());
        bookmark.setSnippet(request.snippet());
        return toDto(bookmarkRepository.save(bookmark));
    }

    @Transactional
    public void delete(Long userId, Long bookmarkId) {
        Bookmark bookmark = getEntity(userId, bookmarkId);
        bookmark.setDeleted(true);
        bookmarkRepository.save(bookmark);
    }

    @Transactional(readOnly = true)
    public Bookmark getEntity(Long userId, Long bookmarkId) {
        return bookmarkRepository.findByIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new NotFoundException("Bookmark not found"));
    }

    private BookmarkDto toDto(Bookmark bookmark) {
        return new BookmarkDto(
                bookmark.getId(),
                bookmark.getBookHash(),
                bookmark.getChapterIdx(),
                bookmark.getScrollPct(),
                bookmark.getSnippet(),
                bookmark.getCreatedAt(),
                bookmark.getUpdatedAt(),
                bookmark.isDeleted()
        );
    }
}
