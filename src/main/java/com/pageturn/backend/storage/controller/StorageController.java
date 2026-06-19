package com.pageturn.backend.storage.controller;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.security.CurrentUserService;
import com.pageturn.backend.storage.dto.StoredBookResponse;
import com.pageturn.backend.storage.dto.UpsertStoredBookRequest;
import com.pageturn.backend.storage.service.StorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/storage/books")
public class StorageController {

    private final StorageService storageService;
    private final CurrentUserService currentUserService;

    public StorageController(StorageService storageService, CurrentUserService currentUserService) {
        this.storageService = storageService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoredBookResponse>>> list() {
        return ApiResponseFactory.success(HttpStatus.OK, "Stored books retrieved",
                storageService.list(currentUserService.requireCurrentUserId()));
    }

    @GetMapping("/{bookHash}")
    public ResponseEntity<ApiResponse<StoredBookResponse>> get(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash) {
        return ApiResponseFactory.success(HttpStatus.OK, "Stored book retrieved",
                storageService.get(currentUserService.requireCurrentUserId(), bookHash));
    }

    @PutMapping("/{bookHash}")
    public ResponseEntity<ApiResponse<StoredBookResponse>> upsert(
            @PathVariable String bookHash,
            @Valid @RequestBody UpsertStoredBookRequest request) {
        if (!bookHash.equals(request.bookHash())) {
            throw new IllegalArgumentException("Path bookHash must match body bookHash");
        }
        return ApiResponseFactory.success(HttpStatus.OK, "Stored book saved",
                storageService.upsert(currentUserService.requireCurrentUserId(), request));
    }

    @DeleteMapping("/{bookHash}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash) {
        storageService.delete(currentUserService.requireCurrentUserId(), bookHash);
        return ApiResponseFactory.success(HttpStatus.OK, "Stored book deleted");
    }
}
