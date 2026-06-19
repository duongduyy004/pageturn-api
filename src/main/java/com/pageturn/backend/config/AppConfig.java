package com.pageturn.backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.util.unit.DataUnit.MEGABYTES;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppConfig(
        @Valid Jwt jwt,
        @Valid Storage storage,
        @Valid Cors cors
) {

    public record Jwt(
            @NotBlank String secret,
            @Min(1) long accessTokenExpirationMinutes,
            @Min(1) long refreshTokenExpirationDays
    ) {
        public long accessTokenTtlSeconds() {
            return ChronoUnit.MINUTES.getDuration().multipliedBy(accessTokenExpirationMinutes).getSeconds();
        }

        public long refreshTokenTtlSeconds() {
            return ChronoUnit.DAYS.getDuration().multipliedBy(refreshTokenExpirationDays).getSeconds();
        }
    }

    public record Storage(
            @NotBlank String uploadDir,
            @NotBlank String publicDir,
            @DataSizeUnit(MEGABYTES) org.springframework.util.unit.DataSize maxFileSize
    ) {
    }

    public record Cors(
            @NotEmpty List<@NotBlank String> allowedOrigins,
            @NotEmpty List<@NotBlank String> allowedMethods,
            @NotEmpty List<@NotBlank String> allowedHeaders
    ) {
        public List<String> allowedOriginPatterns() {
            return allowedOrigins.stream().filter(StringUtils::hasText).toList();
        }
    }
}
