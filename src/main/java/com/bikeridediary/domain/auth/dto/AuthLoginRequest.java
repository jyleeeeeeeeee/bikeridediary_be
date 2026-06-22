package com.bikeridediary.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank(message = "credential은 필수입니다")
        String credential,

        // Apple 최초 로그인 시 사용자 이름 (이후 로그인에서는 null)
        String name
) {
}
