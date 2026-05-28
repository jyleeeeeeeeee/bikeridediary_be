package com.bikeridediary.global.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Google OAuth2 제공자 구현.
 * 현재: Identity Token (JWT)에서 서명 검증 없이 payload만 추출 (프로토타입)
 * TODO: nimbus-jose-jwt 라이브러리 추가 후 완전한 JWT 검증 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleProvider implements OAuth2Provider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Override
    public OAuth2UserInfo getUserInfo(String identityToken) {
        // TODO: 완전한 JWT 검증 구현 필요
        // 현재는 payload 추출만 수행
        log.warn("Google JWT verification not fully implemented - proceeding without signature validation");

        // 임시 구현: payload에서 사용자 정보 추출 (실제 서명 검증 필요)
        throw new UnsupportedOperationException(
                "Google OAuth2 identity token verification is not yet fully implemented. " +
                "Please add nimbus-jose-jwt dependency and implement complete JWT validation."
        );
    }

    @Override
    public String getProviderName() {
        return "google";
    }
}
