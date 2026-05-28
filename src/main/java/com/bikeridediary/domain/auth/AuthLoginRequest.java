package com.bikeridediary.domain.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 소셜 로그인 요청 DTO.
 * OAuth2 제공자의 Authorization Code를 전달받음.
 */
public record AuthLoginRequest(
        // OAuth2 Authorization Code (Kakao만 사용, Google/Apple은 identity_token 사용)
        @NotBlank(message = "code는 필수입니다")
        String code,

        // Google/Apple identity token (code 대신 사용)
        String identityToken
) {
}
