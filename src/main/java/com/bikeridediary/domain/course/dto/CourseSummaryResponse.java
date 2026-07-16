package com.bikeridediary.domain.course.dto;

import com.bikeridediary.domain.course.entity.CourseEntity;
import com.bikeridediary.domain.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

// 리스트용 DTO — path 제외 (수십 KB 절약)
public record CourseSummaryResponse(
        UUID id,
        String name,
        Integer distanceMeters,
        // 작성자 닉네임 — 시드/큐레이션 코스는 빈 문자열("")
        String authorNickname,
        boolean isPublic,
        boolean ownedByMe,
        boolean isFavorited,
        LocalDateTime createdAt
) {
    public static CourseSummaryResponse from(CourseEntity entity, UUID requestUserId, boolean isFavorited) {
        UserEntity author = entity.getUserEntity();
        return new CourseSummaryResponse(
                entity.getId(),
                entity.getName(),
                entity.getDistanceMeters(),
                author == null ? "" : author.getNickname(),
                entity.isPublic(),
                requestUserId != null && author != null && author.getId().equals(requestUserId),
                isFavorited,
                entity.getCreatedAt()
        );
    }
}