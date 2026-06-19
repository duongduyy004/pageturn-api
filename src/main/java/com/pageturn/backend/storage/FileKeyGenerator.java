package com.pageturn.backend.storage;

import com.pageturn.backend.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class FileKeyGenerator {

    public String userBookKey(Long userId, String bookHash, String extension) {
        return "books/" + requirePositive(userId, "userId") + "/" + requireBookHash(bookHash) + "." + requireBookExtension(extension);
    }

    public String publicBookKey(String bookHash, String extension) {
        return "books/" + requireBookHash(bookHash) + "." + requireBookExtension(extension);
    }

    public String transferFileKey(Long transferId, String bookHash, String extension) {
        return "transfers/" + requirePositive(transferId, "transferId") + "/" + requireBookHash(bookHash) + "." + requireBookExtension(extension);
    }

    public String userCoverKey(Long userId, String bookHash) {
        return "covers/" + requirePositive(userId, "userId") + "/" + requireBookHash(bookHash) + ".jpg";
    }

    public String publicCoverKey(String bookHash) {
        return "covers/" + requireBookHash(bookHash) + ".jpg";
    }

    private long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BadRequestException(fieldName + " must be positive");
        }
        return value;
    }

    private String requireBookHash(String bookHash) {
        if (bookHash == null || !bookHash.matches("^[a-f0-9]{64}$")) {
            throw new BadRequestException("bookHash must be a SHA-256 hex string");
        }
        return bookHash;
    }

    private String requireBookExtension(String extension) {
        if (extension == null) {
            throw new BadRequestException("File extension is required");
        }
        String normalized = extension.trim().toLowerCase();
        return switch (normalized) {
            case "epub", "pdf", "txt" -> normalized;
            default -> throw new BadRequestException("Unsupported file extension: " + extension);
        };
    }
}
