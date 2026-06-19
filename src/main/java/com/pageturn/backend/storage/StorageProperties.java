package com.pageturn.backend.storage;

import com.pageturn.backend.common.exception.BadRequestException;
import com.pageturn.backend.config.AppConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

@Component
public class StorageProperties {

    private static final DataSize MAX_ALLOWED_FILE_SIZE = DataSize.ofMegabytes(50);

    private final Path uploadDir;
    private final Path publicDir;
    private final DataSize maxFileSize;

    public StorageProperties(AppConfig appConfig) {
        this.uploadDir = Path.of(appConfig.storage().uploadDir()).toAbsolutePath().normalize();
        this.publicDir = Path.of(appConfig.storage().publicDir()).toAbsolutePath().normalize();
        this.maxFileSize = appConfig.storage().maxFileSize();
        if (this.maxFileSize.toBytes() > MAX_ALLOWED_FILE_SIZE.toBytes()) {
            throw new BadRequestException("Configured storage max file size cannot exceed 50MB");
        }
    }

    public Path uploadDir() {
        return uploadDir;
    }

    public Path publicDir() {
        return publicDir;
    }

    public DataSize maxFileSize() {
        return maxFileSize;
    }
}
