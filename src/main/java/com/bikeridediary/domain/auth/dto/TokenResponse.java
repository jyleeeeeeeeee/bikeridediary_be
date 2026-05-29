package com.bikeridediary.domain.auth.dto;

// 토큰 갱신 응답 DTO
public record TokenResponse(
        // 새로 발급된 Access Token
        String accessToken,

        // 새로 발급된 Refresh Token
        String refreshToken
) {
}
