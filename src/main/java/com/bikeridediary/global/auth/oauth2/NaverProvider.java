package com.bikeridediary.global.auth.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
@RequiredArgsConstructor
public class NaverProvider implements OAuth2Provider{

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 네이버 OAuth2 API 엔드포인트
    private static final String TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";
    private static final String GRANT_TYPE = "authorization_code";

    @Override
    public OAuth2UserInfo getUserInfo(String credential) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(credential);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            String response = restTemplate
                    .exchange(USER_INFO_URI, HttpMethod.POST, requestEntity, String.class)
                    .getBody();

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
