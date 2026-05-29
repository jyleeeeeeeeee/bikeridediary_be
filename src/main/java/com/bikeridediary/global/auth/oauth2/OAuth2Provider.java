package com.bikeridediary.global.auth.oauth2;

// OAuth2 제공자별 사용자 정보 조회 인터페이스. 각 제공자(Kakao, Google, Apple)는 이 인터페이스를 구현.
public interface OAuth2Provider {

    // Authorization Code 또는 Identity Token으로 사용자 정보 조회.
    // credential: OAuth2 제공자에서 발급한 code 또는 identity_token
    // 반환: 통일된 형식의 사용자 정보
    // 예외: 유효하지 않은 credential 또는 OAuth2 제공자 API 호출 실패
    OAuth2UserInfo getUserInfo(String credential);

    // 이 프로바이더 이름을 반환 (예: "kakao", "google", "apple")
    String getProviderName();
}
