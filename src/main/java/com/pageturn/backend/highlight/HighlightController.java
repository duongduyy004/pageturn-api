package com.pageturn.backend.highlight;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.highlight.dto.CreateHighlightRequest;
import com.pageturn.backend.highlight.dto.HighlightDto;
import com.pageturn.backend.highlight.dto.UpdateHighlightRequest;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
public class HighlightController {

    private final HighlightService highlightService;
    private final CurrentUserService currentUserService;

    public HighlightController(HighlightService highlightService, CurrentUserService currentUserService) {
        this.highlightService = highlightService;
        this.currentUserService = currentUserService;
    }

    @GetMapping({"/api/books/{bookHash}/highlights", "/api/v1/books/{bookHash}/highlights"})
    public ResponseEntity<ApiResponse<List<HighlightDto>>> listByBook(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Highlights retrieved",
                highlightService.listByBook(currentUserService.requireCurrentUserId(), bookHash)
        );
    }

    @PostMapping({"/api/books/{bookHash}/highlights", "/api/v1/books/{bookHash}/highlights"})
    public ResponseEntity<ApiResponse<HighlightDto>> create(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash,
            @Valid @RequestBody CreateHighlightRequest request) {
        if (!bookHash.equals(request.bookHash())) {
            throw new IllegalArgumentException("Path bookHash must match body bookHash");
        }
        return ApiResponseFactory.success(
                HttpStatus.CREATED,
                "Highlight created",
                highlightService.create(currentUserService.requireCurrentUserId(), request)
        );
    }

    @PutMapping({"/api/highlights/{highlightId}", "/api/v1/highlights/{highlightId}"})
    public ResponseEntity<ApiResponse<HighlightDto>> update(
            @PathVariable Long highlightId,
            @Valid @RequestBody UpdateHighlightRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Highlight updated",
                highlightService.update(currentUserService.requireCurrentUserId(), highlightId, request)
        );
    }

    @DeleteMapping({"/api/highlights/{highlightId}", "/api/v1/highlights/{highlightId}"})
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long highlightId) {
        highlightService.delete(currentUserService.requireCurrentUserId(), highlightId);
        return ApiResponseFactory.success(HttpStatus.OK, "Highlight deleted");
    }
}
