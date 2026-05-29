package com.bikeridediary.domain.auth.dto;

import com.bikeridediary.domain.user.dto.UserResponse;

/**
 * 소셜 로그인 응답 DTO.
 */

public record AuthResponse(
        // Access Token (1시간 만료)
        String accessToken,

        // Refresh Token (30일 만료)
        String refreshToken,

        // 로그인한 사용자 정보
        UserResponse user
) {
}
