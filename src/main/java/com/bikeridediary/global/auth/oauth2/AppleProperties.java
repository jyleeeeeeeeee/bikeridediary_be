package com.bikeridediary.global.auth.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple")
public record AppleProperties(
        String clientId
) {
}
