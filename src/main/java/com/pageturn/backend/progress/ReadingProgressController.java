package com.pageturn.backend.progress;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.progress.dto.ProgressDto;
import com.pageturn.backend.progress.dto.SaveProgressRequest;
import com.pageturn.backend.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping({"/api/progress", "/api/v1/progress"})
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;
    private final CurrentUserService currentUserService;

    public ReadingProgressController(ReadingProgressService readingProgressService,
                                     CurrentUserService currentUserService) {
        this.readingProgressService = readingProgressService;
        this.currentUserService = currentUserService;
    }

    @PutMapping("/{bookHash}")
    public ResponseEntity<ApiResponse<ProgressDto>> save(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash,
            @Valid @RequestBody SaveProgressRequest request) {
        if (!bookHash.equals(request.bookHash())) {
            throw new IllegalArgumentException("Path bookHash must match body bookHash");
        }
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Reading progress saved",
                readingProgressService.save(currentUserService.requireCurrentUserId(), request)
        );
    }

    @GetMapping("/{bookHash}")
    public ResponseEntity<ApiResponse<ProgressDto>> get(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Reading progress retrieved",
                readingProgressService.get(currentUserService.requireCurrentUserId(), bookHash)
        );
    }
}
