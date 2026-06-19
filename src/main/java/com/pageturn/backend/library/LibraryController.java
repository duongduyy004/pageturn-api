package com.pageturn.backend.library;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.library.dto.CloudBookDto;
import com.pageturn.backend.library.dto.DownloadBookResponse;
import com.pageturn.backend.library.dto.UpsertBookRequest;
import com.pageturn.backend.library.dto.UploadBookResponse;
import com.pageturn.backend.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping({"/api/library", "/api/v1/library/books"})
public class LibraryController {

    private final LibraryService libraryService;
    private final CurrentUserService currentUserService;

    public LibraryController(LibraryService libraryService, CurrentUserService currentUserService) {
        this.libraryService = libraryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CloudBookDto>>> list() {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Library books retrieved",
                libraryService.list(currentUserService.requireCurrentUserId())
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CloudBookDto>> upsert(@Valid @RequestBody UpsertBookRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Library metadata saved",
                libraryService.upsert(currentUserService.requireCurrentUserId(), request)
        );
    }

    @DeleteMapping("/{bookHash}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash,
            @RequestParam(defaultValue = "true") boolean deletePhysicalFile) {
        libraryService.delete(currentUserService.requireCurrentUserId(), bookHash, deletePhysicalFile);
        return ApiResponseFactory.success(HttpStatus.OK, "Library book deleted");
    }

    @PostMapping(path = "/{bookHash}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadBookResponse>> upload(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash,
            @RequestParam("file") MultipartFile file) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Book uploaded",
                libraryService.upload(currentUserService.requireCurrentUserId(), bookHash, file)
        );
    }

    @GetMapping("/{bookHash}/download")
    public ResponseEntity<Resource> download(
            @PathVariable @Pattern(regexp = "^[a-f0-9]{64}$") String bookHash) {
        DownloadBookResponse download = libraryService.download(currentUserService.requireCurrentUserId(), bookHash);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.fileName())
                        .build()
                        .toString())
                .body(download.resource());
    }
}
