package com.pageturn.backend.sync;

import com.pageturn.backend.bookmark.Bookmark;
import com.pageturn.backend.bookmark.BookmarkService;
import com.pageturn.backend.bookmark.dto.CreateBookmarkRequest;
import com.pageturn.backend.bookmark.dto.UpdateBookmarkRequest;
import com.pageturn.backend.collection.Collection;
import com.pageturn.backend.collection.CollectionService;
import com.pageturn.backend.collection.dto.AddBookToCollectionRequest;
import com.pageturn.backend.collection.dto.CollectionDto;
import com.pageturn.backend.collection.dto.CreateCollectionRequest;
import com.pageturn.backend.collection.dto.UpdateCollectionRequest;
import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.highlight.Highlight;
import com.pageturn.backend.highlight.HighlightService;
import com.pageturn.backend.highlight.dto.CreateHighlightRequest;
import com.pageturn.backend.highlight.dto.UpdateHighlightRequest;
import com.pageturn.backend.progress.ReadingProgress;
import com.pageturn.backend.progress.ReadingProgressService;
import com.pageturn.backend.progress.dto.SaveProgressRequest;
import com.pageturn.backend.sync.dto.SyncBookmarkDto;
import com.pageturn.backend.sync.dto.SyncCollectionDto;
import com.pageturn.backend.sync.dto.SyncHighlightDto;
import com.pageturn.backend.sync.dto.SyncProgressDto;
import com.pageturn.backend.sync.strategy.LastWriteWinsStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncMergeService {

    private final ReadingProgressService readingProgressService;
    private final BookmarkService bookmarkService;
    private final HighlightService highlightService;
    private final CollectionService collectionService;
    private final LastWriteWinsStrategy lastWriteWinsStrategy;

    public SyncMergeService(ReadingProgressService readingProgressService,
                            BookmarkService bookmarkService,
                            HighlightService highlightService,
                            CollectionService collectionService,
                            LastWriteWinsStrategy lastWriteWinsStrategy) {
        this.readingProgressService = readingProgressService;
        this.bookmarkService = bookmarkService;
        this.highlightService = highlightService;
        this.collectionService = collectionService;
        this.lastWriteWinsStrategy = lastWriteWinsStrategy;
    }

    @Transactional
    public void mergeProgress(Long userId, SyncProgressDto dto) {
        ReadingProgress existing = findProgress(userId, dto.bookHash());
        if (!lastWriteWinsStrategy.shouldApply(existing != null ? existing.getUpdatedAt() : null, dto.updatedAt())) {
            return;
        }
        readingProgressService.save(userId, new SaveProgressRequest(dto.bookHash(), dto.chapterIdx(), dto.scrollPct()));
    }

    @Transactional
    public void mergeBookmark(Long userId, SyncBookmarkDto dto) {
        Bookmark existing = dto.id() != null ? findBookmark(userId, dto.id()) : null;
        if (!lastWriteWinsStrategy.shouldApply(existing != null ? existing.getUpdatedAt() : null, dto.updatedAt())) {
            return;
        }

        if (existing == null) {
            bookmarkService.create(userId, new CreateBookmarkRequest(dto.bookHash(), dto.chapterIdx(), dto.scrollPct(), dto.snippet()));
            return;
        }

        bookmarkService.update(userId, existing.getId(), new UpdateBookmarkRequest(dto.chapterIdx(), dto.scrollPct(), dto.snippet()));
        if (dto.isDeleted()) {
            bookmarkService.delete(userId, existing.getId());
        }
    }

    @Transactional
    public void mergeHighlight(Long userId, SyncHighlightDto dto) {
        Highlight existing = dto.id() != null ? findHighlight(userId, dto.id()) : null;
        if (!lastWriteWinsStrategy.shouldApply(existing != null ? existing.getUpdatedAt() : null, dto.updatedAt())) {
            return;
        }

        if (existing == null) {
            highlightService.create(
                    userId,
                    new CreateHighlightRequest(
                            dto.bookHash(),
                            dto.chapterIdx(),
                            dto.startOffset(),
                            dto.endOffset(),
                            dto.textContent(),
                            dto.color(),
                            dto.note()
                    )
            );
            return;
        }

        highlightService.update(
                userId,
                existing.getId(),
                new UpdateHighlightRequest(
                        dto.chapterIdx(),
                        dto.startOffset(),
                        dto.endOffset(),
                        dto.textContent(),
                        dto.color(),
                        dto.note()
                )
        );
        if (dto.isDeleted()) {
            highlightService.delete(userId, existing.getId());
        }
    }

    @Transactional
    public void mergeCollection(Long userId, SyncCollectionDto dto) {
        Collection existing = dto.id() != null ? findCollection(userId, dto.id()) : null;
        if (!lastWriteWinsStrategy.shouldApply(existing != null ? existing.getUpdatedAt() : null, dto.updatedAt())) {
            return;
        }

        CollectionDto current;
        if (existing == null) {
            current = collectionService.create(userId, new CreateCollectionRequest(dto.name(), dto.description()));
        } else {
            current = collectionService.update(userId, existing.getId(), new UpdateCollectionRequest(dto.name(), dto.description()));
        }

        collectionService.replaceBooks(
                userId,
                current.id(),
                dto.books() != null
                        ? dto.books().stream()
                        .map(book -> new CollectionDto.CollectionBookItem(book.bookHash(), book.position()))
                        .toList()
                        : java.util.List.of()
        );
    }

    @Transactional
    public void deleteBookmark(Long userId, Long id) {
        Bookmark existing = findBookmark(userId, id);
        if (existing != null && !existing.isDeleted()) {
            bookmarkService.delete(userId, id);
        }
    }

    @Transactional
    public void deleteHighlight(Long userId, Long id) {
        Highlight existing = findHighlight(userId, id);
        if (existing != null && !existing.isDeleted()) {
            highlightService.delete(userId, id);
        }
    }

    @Transactional
    public void deleteCollection(Long userId, Long id) {
        Collection existing = findCollection(userId, id);
        if (existing != null) {
            collectionService.delete(userId, id);
        }
    }

    private ReadingProgress findProgress(Long userId, String bookHash) {
        try {
            return readingProgressService.getEntity(userId, bookHash);
        } catch (NotFoundException ex) {
            return null;
        }
    }

    private Bookmark findBookmark(Long userId, Long id) {
        try {
            return bookmarkService.getEntity(userId, id);
        } catch (NotFoundException ex) {
            return null;
        }
    }

    private Highlight findHighlight(Long userId, Long id) {
        try {
            return highlightService.getEntity(userId, id);
        } catch (NotFoundException ex) {
            return null;
        }
    }

    private Collection findCollection(Long userId, Long id) {
        try {
            return collectionService.getCollection(userId, id);
        } catch (NotFoundException ex) {
            return null;
        }
    }
}
