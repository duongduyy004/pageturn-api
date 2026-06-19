package com.pageturn.backend.collection;

import com.pageturn.backend.collection.dto.AddBookToCollectionRequest;
import com.pageturn.backend.collection.dto.CollectionDto;
import com.pageturn.backend.collection.dto.CreateCollectionRequest;
import com.pageturn.backend.collection.dto.UpdateCollectionRequest;
import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping({"/api/collections", "/api/v1/collections"})
public class CollectionController {

    private final CollectionService collectionService;
    private final CurrentUserService currentUserService;

    public CollectionController(CollectionService collectionService, CurrentUserService currentUserService) {
        this.collectionService = collectionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionDto>>> list() {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Collections retrieved",
                collectionService.list(currentUserService.requireCurrentUserId())
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionDto>> create(@Valid @RequestBody CreateCollectionRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.CREATED,
                "Collection created",
                collectionService.create(currentUserService.requireCurrentUserId(), request)
        );
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<CollectionDto>> update(
            @PathVariable Long collectionId,
            @Valid @RequestBody UpdateCollectionRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Collection updated",
                collectionService.update(currentUserService.requireCurrentUserId(), collectionId, request)
        );
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long collectionId) {
        collectionService.delete(currentUserService.requireCurrentUserId(), collectionId);
        return ApiResponseFactory.success(HttpStatus.OK, "Collection deleted");
    }

    @PostMapping("/{collectionId}/books")
    public ResponseEntity<ApiResponse<CollectionDto>> addBook(
            @PathVariable Long collectionId,
            @Valid @RequestBody AddBookToCollectionRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Book added to collection",
                collectionService.addBook(currentUserService.requireCurrentUserId(), collectionId, request)
        );
    }

    @DeleteMapping("/{collectionId}/books/{bookHash}")
    public ResponseEntity<ApiResponse<CollectionDto>> removeBook(
            @PathVariable Long collectionId,
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Book removed from collection",
                collectionService.removeBook(currentUserService.requireCurrentUserId(), collectionId, bookHash)
        );
    }
}
