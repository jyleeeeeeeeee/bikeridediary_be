package com.bikeridediary.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
public record FileStorageProperties(
        String uploadDir,
        String baseUrl
) {
}
