package com.bikeridediary.global.auth.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.oauth2")
public record NaverOAuth2Properties(
        String clientId,
        String clientSecret
) {
}
