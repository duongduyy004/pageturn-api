package com.pageturn.backend.library.mapper;

import com.pageturn.backend.library.UserCloudBook;
import com.pageturn.backend.library.dto.CloudBookDto;
import com.pageturn.backend.library.dto.DownloadBookResponse;
import com.pageturn.backend.library.dto.UploadBookResponse;
import com.pageturn.backend.storage.dto.FileDownloadResult;
import com.pageturn.backend.storage.dto.StoredFile;
import org.springframework.stereotype.Component;

@Component
public class LibraryMapper {

    public CloudBookDto toCloudBookDto(UserCloudBook book) {
        return new CloudBookDto(
                book.getBookHash(),
                book.getTitle(),
                book.getAuthor(),
                book.getFileFormat(),
                book.getCoverKey(),
                book.getFileKey(),
                book.getFileSize(),
                book.isCloudSynced(),
                book.getAddedAt(),
                book.getUpdatedAt()
        );
    }

    public UploadBookResponse toUploadBookResponse(UserCloudBook book, StoredFile storedFile) {
        return new UploadBookResponse(
                book.getBookHash(),
                storedFile.fileKey(),
                book.getFileFormat(),
                storedFile.fileSize(),
                book.isCloudSynced()
        );
    }

    public DownloadBookResponse toDownloadBookResponse(UserCloudBook book, FileDownloadResult result) {
        return new DownloadBookResponse(
                book.getBookHash(),
                result.fileName(),
                result.contentType() != null ? result.contentType() : "application/octet-stream",
                result.contentLength(),
                result.resource()
        );
    }
}
