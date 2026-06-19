package com.pageturn.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class PageTurnBackendApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(PageTurnBackendApplication.class, args);
    }

    private static void loadDotEnvIfPresent() {
        Path envFile = Path.of(".env").toAbsolutePath().normalize();
        if (!Files.exists(envFile) || !Files.isRegularFile(envFile)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, separatorIndex).trim();
                String value = stripQuotes(line.substring(separatorIndex + 1).trim());

                if (key.isEmpty()) {
                    continue;
                }
                if (System.getenv(key) != null || System.getProperty(key) != null) {
                    continue;
                }

                System.setProperty(key, value);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load .env file", ex);
        }
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
