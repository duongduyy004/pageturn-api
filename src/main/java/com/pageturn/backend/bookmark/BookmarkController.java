package com.pageturn.backend.bookmark;

import com.pageturn.backend.bookmark.dto.BookmarkDto;
import com.pageturn.backend.bookmark.dto.CreateBookmarkRequest;
import com.pageturn.backend.bookmark.dto.UpdateBookmarkRequest;
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
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final CurrentUserService currentUserService;

    public BookmarkController(BookmarkService bookmarkService, CurrentUserService currentUserService) {
        this.bookmarkService = bookmarkService;
        this.currentUserService = currentUserService;
    }

    @GetMapping({"/api/books/{bookHash}/bookmarks", "/api/v1/books/{bookHash}/bookmarks"})
    public ResponseEntity<ApiResponse<List<BookmarkDto>>> listByBook(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Bookmarks retrieved",
                bookmarkService.listByBook(currentUserService.requireCurrentUserId(), bookHash)
        );
    }

    @PostMapping({"/api/books/{bookHash}/bookmarks", "/api/v1/books/{bookHash}/bookmarks"})
    public ResponseEntity<ApiResponse<BookmarkDto>> create(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash,
            @Valid @RequestBody CreateBookmarkRequest request) {
        if (!bookHash.equals(request.bookHash())) {
            throw new IllegalArgumentException("Path bookHash must match body bookHash");
        }
        return ApiResponseFactory.success(
                HttpStatus.CREATED,
                "Bookmark created",
                bookmarkService.create(currentUserService.requireCurrentUserId(), request)
        );
    }

    @PutMapping({"/api/bookmarks/{bookmarkId}", "/api/v1/bookmarks/{bookmarkId}"})
    public ResponseEntity<ApiResponse<BookmarkDto>> update(
            @PathVariable Long bookmarkId,
            @Valid @RequestBody UpdateBookmarkRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Bookmark updated",
                bookmarkService.update(currentUserService.requireCurrentUserId(), bookmarkId, request)
        );
    }

    @DeleteMapping({"/api/bookmarks/{bookmarkId}", "/api/v1/bookmarks/{bookmarkId}"})
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long bookmarkId) {
        bookmarkService.delete(currentUserService.requireCurrentUserId(), bookmarkId);
        return ApiResponseFactory.success(HttpStatus.OK, "Bookmark deleted");
    }
}
