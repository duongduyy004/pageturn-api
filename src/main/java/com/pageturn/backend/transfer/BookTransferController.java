package com.pageturn.backend.transfer;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.security.CurrentUserService;
import com.pageturn.backend.transfer.dto.AcceptTransferResponse;
import com.pageturn.backend.transfer.dto.DeclineTransferResponse;
import com.pageturn.backend.transfer.dto.SendBookRequest;
import com.pageturn.backend.transfer.dto.TransferDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/transfers", "/api/v1/transfers"})
public class BookTransferController {

    private final BookTransferService bookTransferService;
    private final CurrentUserService currentUserService;

    public BookTransferController(BookTransferService bookTransferService,
                                  CurrentUserService currentUserService) {
        this.bookTransferService = bookTransferService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransferDto>> send(@Valid @RequestBody SendBookRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.CREATED,
                "Book transfer created",
                bookTransferService.send(currentUserService.requireCurrentUserId(), request)
        );
    }

    @GetMapping("/inbox")
    public ResponseEntity<ApiResponse<List<TransferDto>>> inbox() {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Transfer inbox retrieved",
                bookTransferService.getInbox(currentUserService.requireCurrentUserId())
        );
    }

    @PutMapping("/{transferId}/accept")
    public ResponseEntity<ApiResponse<AcceptTransferResponse>> accept(@PathVariable Long transferId) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Transfer accepted",
                bookTransferService.accept(currentUserService.requireCurrentUserId(), transferId)
        );
    }

    @PutMapping("/{transferId}/decline")
    public ResponseEntity<ApiResponse<DeclineTransferResponse>> decline(@PathVariable Long transferId) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Transfer declined",
                bookTransferService.decline(currentUserService.requireCurrentUserId(), transferId)
        );
    }
}
