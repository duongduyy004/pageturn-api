package com.pageturn.backend.store;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.security.CurrentUserService;
import com.pageturn.backend.store.dto.CreatePublicBookRequest;
import com.pageturn.backend.store.dto.PublicBookDetailDto;
import com.pageturn.backend.store.dto.UpdatePublicBookRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping({"/api/admin/store", "/api/v1/admin/store"})
public class AdminStoreController {

    private final PublicBookService publicBookService;
    private final CurrentUserService currentUserService;

    public AdminStoreController(PublicBookService publicBookService,
                                CurrentUserService currentUserService) {
        this.publicBookService = publicBookService;
        this.currentUserService = currentUserService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PublicBookDetailDto>> create(
            @Valid @ModelAttribute CreatePublicBookRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.CREATED,
                "Public book created",
                publicBookService.create(currentUserService.requireCurrentUserId(), request)
        );
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PublicBookDetailDto>> update(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdatePublicBookRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Public book updated",
                publicBookService.update(id, request)
        );
    }
}
