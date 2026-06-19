package com.pageturn.backend.storage;

import com.pageturn.backend.storage.dto.FileDownloadResult;
import com.pageturn.backend.storage.dto.StoredFile;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StoredFile saveUserBook(Long userId, String bookHash, MultipartFile file);

    StoredFile savePublicBook(String bookHash, MultipartFile file);

    StoredFile saveTransferFile(Long transferId, String bookHash, MultipartFile file);

    StoredFile copyUploadFileToTransfer(Long transferId, String sourceFileKey, String bookHash);

    StoredFile saveUserCoverImage(Long userId, String bookHash, MultipartFile file);

    StoredFile savePublicCoverImage(String bookHash, MultipartFile file);

    FileDownloadResult downloadUploadFile(String fileKey);

    FileDownloadResult downloadPublicFile(String fileKey);

    void deleteUploadFile(String fileKey);

    void deletePublicFile(String fileKey);
}
