package com.bikeridediary.infra.naver.drive;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.search")
public record NaverDriveProperties(
        String clientId,
        String clientSecret,
        String url
) {
}
