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


@Slf4j
@Component
@RequiredArgsConstructor
public class NaverProvider implements OAuth2Provider{

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    // 네이버 OAuth2 API 엔드포인트
    private static final String TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";
    private static final String GRANT_TYPE = "authorization_code";

    @Override
    public OAuth2UserInfo getUserInfo(String credential) {
        // 1단계: Authorization Code로 Access Token 요청
        String accessToken = getAccessToken(credential);

        // 2단계: Access Token으로 사용자 정보 조회
        return getUserInfoByAccessToken(accessToken);
    }

    // Authorization Code로 Access Token 요청
    private String getAccessToken(String code) {
        try {
            String body = String.format(
                    "grant_type=%s&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                    GRANT_TYPE,
                    clientId,
                    clientSecret,
                    code,
                    "http://localhost:8080/api/v1/auth/naver/callback"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(TOKEN_URI, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response);
            String accessToken = jsonNode.get("access_token").asText();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("네이버 액세스 토큰을 찾을 수 없음");
            }

            return accessToken;
        } catch (Exception e) {
            log.error("네이버 액세스 토큰 요청 실패", e);
            throw new RuntimeException("네이버 로그인 실패: 토큰 획득 오류", e);
        }
    }

    private OAuth2UserInfo getUserInfoByAccessToken(String accessToken) {
        /*{
            "resultcode": "00",
            "message": "success",
            "response": {
                "id": "naver_123456789",
                "nickname": "...",
                "name": "...",
                "email": "...",
                "profile_image": "..."
            }
        }*/
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            String response = restTemplate.postForObject(USER_INFO_URI, request, String.class);

            JsonNode naverAccount = objectMapper.readTree(response).get("response");

            Long id = getProperty(naverAccount, Long.class, "id");
            String email = getProperty(naverAccount, String.class, "email");
            String nickname = getProperty(naverAccount, String.class, "nickname");
            String profileImageUrl = getProperty(naverAccount, String.class, "profile_image");

            return OAuth2UserInfo.fromNaver(id, email, nickname, profileImageUrl);

        } catch (Exception e) {
            log.error("네이버 사용자 정보 요청 실패", e);
            throw new RuntimeException("네이버 실패: 사용자 정보 조회 오류", e);
        }
    }

    @Override
    public String getProviderName() {
        return "naver";
    }

    private <T> T getProperty(JsonNode jsonNode, Class<T> clazz, String property) {
        if(jsonNode == null || !jsonNode.has(property)) return null;

        JsonNode data = jsonNode.get(property);

        if (clazz == String.class)  return clazz.cast(data.asText());
        if (clazz == Long.class)    return clazz.cast(data.asLong());
        if (clazz == Integer.class) return clazz.cast(data.asInt());
        if (clazz == Boolean.class) return clazz.cast(data.asBoolean());

        return null;
    }
}
