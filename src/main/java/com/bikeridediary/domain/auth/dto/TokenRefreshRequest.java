package com.bikeridediary.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

// 토큰 갱신 요청 DTO
public record TokenRefreshRequest(
        // 유효한 Refresh Token
        @NotBlank(message = "refreshToken은 필수입니다")
        String refreshToken
) {
}
