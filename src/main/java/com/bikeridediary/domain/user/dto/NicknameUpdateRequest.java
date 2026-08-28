package com.bikeridediary.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
        @NotBlank(message = "닉네임을 입력하세요")
        @Size(min = 2, max = 20, message = "닉네임은 2 ~ 20자 이내로 입력해주세요")
        String nickname
) {
}
