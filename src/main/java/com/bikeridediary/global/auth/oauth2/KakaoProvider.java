package com.bikeridediary.global.auth.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoProvider implements OAuth2Provider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            String response = restTemplate.postForObject(USER_INFO_URI, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response);

            Long id = jsonNode.get("id").asLong();

            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            String email = kakaoAccount != null && kakaoAccount.has("email")
                    ? kakaoAccount.get("email").asText()
                    : null;

            String nickname = null;
            String profileImageUrl = null;
            if (kakaoAccount != null && kakaoAccount.has("profile")) {
                JsonNode profile = kakaoAccount.get("profile");
                nickname = profile.has("nickname") ? profile.get("nickname").asText() : null;
                profileImageUrl = profile.has("profile_image_url") ? profile.get("profile_image_url").asText() : null;
            }

            return OAuth2UserInfo.fromKakao(id, email, nickname, profileImageUrl);
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패", e);
            throw new RuntimeException("카카오 로그인 실패: 사용자 정보 조회 오류", e);
        }
    }

    @Override
    public String getProviderName() {
        return "kakao";
    }
}
