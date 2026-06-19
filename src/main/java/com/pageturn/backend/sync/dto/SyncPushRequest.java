package com.pageturn.backend.sync.dto;

import jakarta.validation.Valid;

import java.util.List;

public record SyncPushRequest(
        List<@Valid SyncProgressDto> progress,
        List<@Valid SyncBookmarkDto> bookmarks,
        List<@Valid SyncHighlightDto> highlights,
        List<@Valid SyncCollectionDto> collections
) {
}
