package com.bikeridediary.global.auth.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Spring Security OAuth2 client registration의 google 항목만 뽑아 사용.
// Spring 자체 OAuth2ClientProperties와 프리픽스가 겹치지만 병행 바인딩 가능.
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
public record GoogleOAuth2Properties(
        String clientId
) {
}
