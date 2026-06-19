package com.pageturn.backend.sync.strategy;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LastWriteWinsStrategy {

    public boolean shouldApply(Instant existingUpdatedAt, Instant incomingUpdatedAt) {
        if (incomingUpdatedAt == null) {
            return existingUpdatedAt == null;
        }
        return existingUpdatedAt == null || incomingUpdatedAt.isAfter(existingUpdatedAt);
    }
}
