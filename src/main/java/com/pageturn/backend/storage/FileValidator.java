package com.pageturn.backend.storage;

import com.pageturn.backend.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class FileValidator {

    private static final Map<String, Set<String>> ALLOWED_BOOK_MIME_TYPES = Map.of(
            "epub", Set.of("application/epub+zip", "application/octet-stream"),
            "pdf", Set.of("application/pdf", "application/octet-stream"),
            "txt", Set.of("text/plain", "application/octet-stream")
    );

    private static final Set<String> ALLOWED_COVER_MIME_TYPES = Set.of("image/jpeg", "image/jpg", "image/pjpeg");

    private final StorageProperties storageProperties;

    public FileValidator(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public String validateBookFile(MultipartFile file) {
        validateNotEmpty(file);
        validateFileSize(file);
        String extension = extractExtension(file.getOriginalFilename());
        Set<String> mimeTypes = ALLOWED_BOOK_MIME_TYPES.get(extension);
        if (mimeTypes == null) {
            throw new BadRequestException("Invalid file extension. Allowed extensions: epub, pdf, txt");
        }
        validateMimeType(file.getContentType(), mimeTypes, "Invalid MIME type for ." + extension + " file");
        return extension;
    }

    public void validateCoverImage(MultipartFile file) {
        validateNotEmpty(file);
        validateFileSize(file);
        String extension = extractExtension(file.getOriginalFilename());
        if (!"jpg".equals(extension) && !"jpeg".equals(extension)) {
            throw new BadRequestException("Invalid cover image extension. Only jpg is allowed");
        }
        validateMimeType(file.getContentType(), ALLOWED_COVER_MIME_TYPES, "Invalid MIME type for cover image");
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > storageProperties.maxFileSize().toBytes()) {
            throw new BadRequestException("File exceeds maximum size of 50MB");
        }
    }

    private void validateMimeType(String contentType, Set<String> allowed, String message) {
        if (contentType == null) {
            throw new BadRequestException(message);
        }
        String normalized = contentType.toLowerCase(Locale.ROOT);
        if (allowed.stream().noneMatch(allowedType -> normalized.equals(allowedType) || normalized.startsWith(allowedType + ";"))) {
            throw new BadRequestException(message);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("Original filename is required");
        }
        String normalized = fileName.replace("\\", "/");
        String baseName = normalized.substring(normalized.lastIndexOf('/') + 1);
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == baseName.length() - 1) {
            throw new BadRequestException("Uploaded file must have a valid extension");
        }
        return baseName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
