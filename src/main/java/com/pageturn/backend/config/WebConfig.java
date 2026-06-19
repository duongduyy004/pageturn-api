package com.pageturn.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppConfig appConfig;

    public WebConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path publicDir = Path.of(appConfig.storage().publicDir()).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(publicDir.toUri().toString());
    }
}
