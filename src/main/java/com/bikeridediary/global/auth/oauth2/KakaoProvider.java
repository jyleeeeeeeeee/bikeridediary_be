package com.bikeridediary.global.auth.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Kakao OAuth2 제공자 구현.
 * 1. Authorization Code로 Access Token 요청
 * 2. Access Token으로 사용자 정보 조회
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoProvider implements OAuth2Provider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String GRANT_TYPE = "authorization_code";

    @Override
    public OAuth2UserInfo getUserInfo(String code) {
        // Step 1: Authorization Code로 Access Token 요청
        String accessToken = getAccessToken(code);

        // Step 2: Access Token으로 사용자 정보 조회
        return getUserInfoByAccessToken(accessToken);
    }

    /**
     * Authorization Code로 Access Token 요청.
     */
    private String getAccessToken(String code) {
        try {
            String body = String.format(
                    "grant_type=%s&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                    GRANT_TYPE,
                    clientId,
                    clientSecret,
                    code,
                    "http://localhost:8080/api/v1/auth/kakao/callback"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(TOKEN_URI, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response);
            String accessToken = jsonNode.get("access_token").asText();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Kakao access token not found");
            }

            return accessToken;
        } catch (Exception e) {
            log.error("Kakao access token request failed", e);
            throw new RuntimeException("카카오 로그인 실패: 토큰 획득 오류", e);
        }
    }

    /**
     * Access Token으로 사용자 정보 조회.
     */
    private OAuth2UserInfo getUserInfoByAccessToken(String accessToken) {
        /*{
            "id": 123456789,
            "kakao_account": {
                "email": "...",
                "profile": {
                    "nickname": "...",
                    "profile_image_url": "..."
                }
            }
        }*/
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            String response = restTemplate.postForObject(USER_INFO_URI, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response);

            // 사용자 ID
            Long id = jsonNode.get("id").asLong();

            // 이메일 (kakao_account 안에 있음)
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            String email = kakaoAccount != null && kakaoAccount.has("email")
                    ? kakaoAccount.get("email").asText()
                    : null;

            // 닉네임 & 프로필 이미지 (profile 안에 있음)
            String nickname = null;
            String profileImageUrl = null;
            if (kakaoAccount != null && kakaoAccount.has("profile")) {
                JsonNode profile = kakaoAccount.get("profile");
                nickname = profile.has("nickname") ? profile.get("nickname").asText() : null;
                profileImageUrl = profile.has("profile_image_url") ? profile.get("profile_image_url").asText() : null;
            }

            return OAuth2UserInfo.fromKakao(id, email, nickname, profileImageUrl);
        } catch (Exception e) {
            log.error("Kakao user info request failed", e);
            throw new RuntimeException("카카오 로그인 실패: 사용자 정보 조회 오류", e);
        }
    }

    @Override
    public String getProviderName() {
        return "kakao";
    }
}
