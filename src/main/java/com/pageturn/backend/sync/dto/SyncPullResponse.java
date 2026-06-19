package com.pageturn.backend.sync.dto;

import java.time.Instant;
import java.util.List;

public record SyncPullResponse(
        Instant serverTime,
        List<SyncProgressDto> progress,
        List<SyncBookmarkDto> bookmarks,
        List<SyncHighlightDto> highlights,
        List<SyncCollectionDto> collections
) {
}
