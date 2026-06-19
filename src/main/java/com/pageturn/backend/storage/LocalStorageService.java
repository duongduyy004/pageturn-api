package com.pageturn.backend.storage;

import com.pageturn.backend.common.exception.BadRequestException;
import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.storage.dto.FileDownloadResult;
import com.pageturn.backend.storage.dto.StoredFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class LocalStorageService implements StorageService {

    private final StorageProperties storageProperties;
    private final FileValidator fileValidator;
    private final FileKeyGenerator fileKeyGenerator;

    public LocalStorageService(StorageProperties storageProperties,
                               FileValidator fileValidator,
                               FileKeyGenerator fileKeyGenerator) {
        this.storageProperties = storageProperties;
        this.fileValidator = fileValidator;
        this.fileKeyGenerator = fileKeyGenerator;
    }

    @Override
    public StoredFile saveUserBook(Long userId, String bookHash, MultipartFile file) {
        String extension = fileValidator.validateBookFile(file);
        String fileKey = fileKeyGenerator.userBookKey(userId, bookHash, extension);
        return save(file, storageProperties.uploadDir(), fileKey, false);
    }

    @Override
    public StoredFile savePublicBook(String bookHash, MultipartFile file) {
        String extension = fileValidator.validateBookFile(file);
        String fileKey = fileKeyGenerator.publicBookKey(bookHash, extension);
        return save(file, storageProperties.publicDir(), fileKey, true);
    }

    @Override
    public StoredFile saveTransferFile(Long transferId, String bookHash, MultipartFile file) {
        String extension = fileValidator.validateBookFile(file);
        String fileKey = fileKeyGenerator.transferFileKey(transferId, bookHash, extension);
        return save(file, storageProperties.uploadDir(), fileKey, false);
    }

    @Override
    public StoredFile copyUploadFileToTransfer(Long transferId, String sourceFileKey, String bookHash) {
        Path source = resolveSafePath(storageProperties.uploadDir(), sourceFileKey);
        if (!Files.exists(source) || !Files.isRegularFile(source)) {
            throw new NotFoundException("Stored file not found");
        }

        String extension = extractExtension(sourceFileKey);
        String fileKey = fileKeyGenerator.transferFileKey(transferId, bookHash, extension);
        Path target = resolveSafePath(storageProperties.uploadDir(), fileKey);

        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(
                    fileKey,
                    target.getFileName().toString(),
                    normalizeContentType(Files.probeContentType(target)),
                    Files.size(target),
                    false,
                    target.toAbsolutePath().toString()
            );
        } catch (IOException ex) {
            throw new BadRequestException("Failed to copy stored file");
        }
    }

    @Override
    public StoredFile saveUserCoverImage(Long userId, String bookHash, MultipartFile file) {
        fileValidator.validateCoverImage(file);
        String fileKey = fileKeyGenerator.userCoverKey(userId, bookHash);
        return save(file, storageProperties.uploadDir(), fileKey, false);
    }

    @Override
    public StoredFile savePublicCoverImage(String bookHash, MultipartFile file) {
        fileValidator.validateCoverImage(file);
        String fileKey = fileKeyGenerator.publicCoverKey(bookHash);
        return save(file, storageProperties.publicDir(), fileKey, true);
    }

    @Override
    public FileDownloadResult downloadUploadFile(String fileKey) {
        return download(storageProperties.uploadDir(), fileKey);
    }

    @Override
    public FileDownloadResult downloadPublicFile(String fileKey) {
        return download(storageProperties.publicDir(), fileKey);
    }

    @Override
    public void deleteUploadFile(String fileKey) {
        delete(storageProperties.uploadDir(), fileKey);
    }

    @Override
    public void deletePublicFile(String fileKey) {
        delete(storageProperties.publicDir(), fileKey);
    }

    private StoredFile save(MultipartFile file, Path root, String fileKey, boolean publicFile) {
        Path target = resolveSafePath(root, fileKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(
                    fileKey,
                    file.getOriginalFilename(),
                    normalizeContentType(file.getContentType()),
                    file.getSize(),
                    publicFile,
                    target.toAbsolutePath().toString()
            );
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store file: " + file.getOriginalFilename());
        }
    }

    private FileDownloadResult download(Path root, String fileKey) {
        Path target = resolveSafePath(root, fileKey);
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new NotFoundException("Stored file not found");
        }

        try {
            Resource resource = new FileSystemResource(target);
            return new FileDownloadResult(
                    resource,
                    Files.probeContentType(target),
                    Files.size(target),
                    target.getFileName().toString()
            );
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read stored file");
        }
    }

    private void delete(Path root, String fileKey) {
        Path target = resolveSafePath(root, fileKey);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to delete stored file");
        }
    }

    private Path resolveSafePath(Path root, String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            throw new BadRequestException("File key is required");
        }
        if (fileKey.contains("..")) {
            throw new BadRequestException("Invalid file key");
        }

        Path resolved = root.resolve(fileKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new BadRequestException("Invalid file key");
        }
        return resolved;
    }

    private String normalizeContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
    }

    private String extractExtension(String fileKey) {
        int dotIndex = fileKey.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileKey.length() - 1) {
            throw new BadRequestException("Stored file extension is invalid");
        }
        return fileKey.substring(dotIndex + 1);
    }
}
