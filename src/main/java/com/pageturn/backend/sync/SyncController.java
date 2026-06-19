package com.pageturn.backend.sync;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.security.CurrentUserService;
import com.pageturn.backend.sync.dto.SyncDeleteRequest;
import com.pageturn.backend.sync.dto.SyncPullResponse;
import com.pageturn.backend.sync.dto.SyncPushRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping({"/api/sync", "/api/v1/sync"})
public class SyncController {

    private final SyncService syncService;
    private final CurrentUserService currentUserService;

    public SyncController(SyncService syncService, CurrentUserService currentUserService) {
        this.syncService = syncService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/push")
    public ResponseEntity<ApiResponse<Void>> push(@Valid @RequestBody SyncPushRequest request) {
        syncService.push(currentUserService.requireCurrentUserId(), request);
        return ApiResponseFactory.success(HttpStatus.OK, "Sync push succeeded");
    }

    @GetMapping("/pull")
    public ResponseEntity<ApiResponse<SyncPullResponse>> pull(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Sync pull succeeded",
                syncService.pull(currentUserService.requireCurrentUserId(), since)
        );
    }

    @PostMapping("/deletes")
    public ResponseEntity<ApiResponse<Void>> deletes(@Valid @RequestBody SyncDeleteRequest request) {
        syncService.applyDeletes(currentUserService.requireCurrentUserId(), request);
        return ApiResponseFactory.success(HttpStatus.OK, "Sync deletes succeeded");
    }
}
