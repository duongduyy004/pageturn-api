package com.pageturn.backend.sync.dto;

import java.util.List;

public record SyncDeleteRequest(
        List<Long> bookmarkIds,
        List<Long> highlightIds,
        List<Long> collectionIds
) {
}
