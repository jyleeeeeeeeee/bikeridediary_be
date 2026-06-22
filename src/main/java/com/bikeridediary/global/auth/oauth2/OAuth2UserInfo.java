package com.bikeridediary.global.auth.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// OAuth2 제공자(카카오, 구글, Apple)의 사용자 정보를 통일된 형식으로 표현.
// 각 제공자의 응답 형식을 이 포맷으로 변환.
@Getter
@Setter
@AllArgsConstructor
public class OAuth2UserInfo {

    // 제공자 고유 ID (providerId)
    private String id;

    // 이메일
    private String email;

    // 사용자명/닉네임
    private String name;

    // 프로필 이미지 URL
    private String picture;

    // 카카오 응답 데이터로 생성
    public static OAuth2UserInfo fromKakao(Long id, String email, String nickname, String profileImageUrl) {
        return new OAuth2UserInfo(
                id.toString(),
                email,
                nickname,
                profileImageUrl
        );
    }

    // 구글 응답 데이터로 생성
    public static OAuth2UserInfo fromGoogle(String sub, String email, String name, String picture) {
        return new OAuth2UserInfo(sub, email, name, picture);
    }

    // Apple 응답 데이터로 생성. Apple은 사용자명/프로필 이미지를 제공하지 않음.
    public static OAuth2UserInfo fromApple(String sub, String email) {
        return new OAuth2UserInfo(sub, email, null, null);
    }

    // 네이버 응답 데이터로 생성
    public static OAuth2UserInfo fromNaver(Long id, String email, String nickname, String profileImageUrl) {
        return new OAuth2UserInfo(
                id.toString(),
                email,
                nickname,
                profileImageUrl
        );
    }
}
