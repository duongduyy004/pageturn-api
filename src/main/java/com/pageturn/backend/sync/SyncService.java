package com.pageturn.backend.sync;

import com.pageturn.backend.bookmark.BookmarkService;
import com.pageturn.backend.collection.CollectionService;
import com.pageturn.backend.highlight.HighlightService;
import com.pageturn.backend.progress.ReadingProgressService;
import com.pageturn.backend.sync.dto.SyncBookmarkDto;
import com.pageturn.backend.sync.dto.SyncCollectionDto;
import com.pageturn.backend.sync.dto.SyncDeleteRequest;
import com.pageturn.backend.sync.dto.SyncHighlightDto;
import com.pageturn.backend.sync.dto.SyncProgressDto;
import com.pageturn.backend.sync.dto.SyncPullResponse;
import com.pageturn.backend.sync.dto.SyncPushRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SyncService {

    private final ReadingProgressService readingProgressService;
    private final BookmarkService bookmarkService;
    private final HighlightService highlightService;
    private final CollectionService collectionService;
    private final SyncMergeService syncMergeService;

    public SyncService(ReadingProgressService readingProgressService,
                       BookmarkService bookmarkService,
                       HighlightService highlightService,
                       CollectionService collectionService,
                       SyncMergeService syncMergeService) {
        this.readingProgressService = readingProgressService;
        this.bookmarkService = bookmarkService;
        this.highlightService = highlightService;
        this.collectionService = collectionService;
        this.syncMergeService = syncMergeService;
    }

    @Transactional
    public void push(Long userId, SyncPushRequest request) {
        if (request.progress() != null) {
            request.progress().forEach(dto -> syncMergeService.mergeProgress(userId, dto));
        }
        if (request.bookmarks() != null) {
            request.bookmarks().forEach(dto -> syncMergeService.mergeBookmark(userId, dto));
        }
        if (request.highlights() != null) {
            request.highlights().forEach(dto -> syncMergeService.mergeHighlight(userId, dto));
        }
        if (request.collections() != null) {
            request.collections().forEach(dto -> syncMergeService.mergeCollection(userId, dto));
        }
    }

    @Transactional(readOnly = true)
    public SyncPullResponse pull(Long userId, Instant since) {
        Instant effectiveSince = since != null ? since : Instant.EPOCH;
        return new SyncPullResponse(
                Instant.now(),
                toSyncProgressDtos(readingProgressService.listUpdatedAfter(userId, effectiveSince)),
                toSyncBookmarkDtos(bookmarkService.listUpdatedAfter(userId, effectiveSince)),
                toSyncHighlightDtos(highlightService.listUpdatedAfter(userId, effectiveSince)),
                toSyncCollectionDtos(collectionService.listUpdatedAfter(userId, effectiveSince))
        );
    }

    @Transactional
    public void applyDeletes(Long userId, SyncDeleteRequest request) {
        if (request.bookmarkIds() != null) {
            request.bookmarkIds().forEach(id -> syncMergeService.deleteBookmark(userId, id));
        }
        if (request.highlightIds() != null) {
            request.highlightIds().forEach(id -> syncMergeService.deleteHighlight(userId, id));
        }
        if (request.collectionIds() != null) {
            request.collectionIds().forEach(id -> syncMergeService.deleteCollection(userId, id));
        }
    }

    private List<SyncProgressDto> toSyncProgressDtos(List<com.pageturn.backend.progress.dto.ProgressDto> progressDtos) {
        return progressDtos.stream()
                .map(dto -> new SyncProgressDto(dto.bookHash(), dto.chapterIdx(), dto.scrollPct(), dto.updatedAt()))
                .toList();
    }

    private List<SyncBookmarkDto> toSyncBookmarkDtos(List<com.pageturn.backend.bookmark.dto.BookmarkDto> bookmarkDtos) {
        return bookmarkDtos.stream()
                .map(dto -> new SyncBookmarkDto(
                        dto.id(),
                        dto.bookHash(),
                        dto.chapterIdx(),
                        dto.scrollPct(),
                        dto.snippet(),
                        dto.updatedAt(),
                        dto.isDeleted()
                ))
                .toList();
    }

    private List<SyncHighlightDto> toSyncHighlightDtos(List<com.pageturn.backend.highlight.dto.HighlightDto> highlightDtos) {
        return highlightDtos.stream()
                .map(dto -> new SyncHighlightDto(
                        dto.id(),
                        dto.bookHash(),
                        dto.chapterIdx(),
                        dto.startOffset(),
                        dto.endOffset(),
                        dto.textContent(),
                        dto.color(),
                        dto.note(),
                        dto.updatedAt(),
                        dto.isDeleted()
                ))
                .toList();
    }

    private List<SyncCollectionDto> toSyncCollectionDtos(List<com.pageturn.backend.collection.dto.CollectionDto> collectionDtos) {
        return collectionDtos.stream()
                .map(dto -> new SyncCollectionDto(
                        dto.id(),
                        dto.name(),
                        dto.description(),
                        dto.books().stream()
                                .map(book -> new SyncCollectionDto.SyncCollectionBookItem(book.bookHash(), book.position()))
                                .toList(),
                        dto.updatedAt()
                ))
                .toList();
    }
}
