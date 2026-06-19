package com.pageturn.backend.store;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.storage.dto.FileDownloadResult;
import com.pageturn.backend.store.dto.PublicBookDetailDto;
import com.pageturn.backend.store.dto.PublicBookDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/store", "/api/v1/store"})
public class PublicStoreController {

    private final PublicBookService publicBookService;

    public PublicStoreController(PublicBookService publicBookService) {
        this.publicBookService = publicBookService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<PublicBookDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String keyword) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Public books retrieved",
                publicBookService.listActive(category, keyword, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicBookDetailDto>> get(@PathVariable Long id) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Public book retrieved",
                publicBookService.getActive(id)
        );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileDownloadResult download = publicBookService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(normalizeContentType(download.contentType())))
                .contentLength(download.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.fileName())
                        .build()
                        .toString())
                .body(download.resource());
    }

    private String normalizeContentType(String contentType) {
        return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
