package com.bikeridediary.domain.user;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 정보 응답 DTO.
 */
public record UserResponse(
        // 사용자 ID
        UUID id,

        // OAuth2 제공자 (kakao, google, apple)
        String provider,

        // 닉네임
        String nickname,

        // 이메일
        String email,

        // 프로필 이미지 URL
        String profileImageUrl,

        // 가입 일시
        LocalDateTime createdAt
) {

    /**
     * User 엔티티로부터 응답 DTO 생성.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getProvider(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getCreatedAt()
        );
    }
}
