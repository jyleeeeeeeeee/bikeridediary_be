package com.bikeridediary.domain.course.dto;

import com.bikeridediary.domain.course.entity.CourseEntity;
import com.bikeridediary.domain.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CourseDetailResponse(
        UUID id,
        String name,
        Integer distanceMeters,
        // 작성자 닉네임 — 시드/큐레이션 코스는 빈 문자열("")
        String authorNickname,
        String path,
        boolean isPublic,
        UUID sourceCourseId,
        List<CourseWaypointResponse> waypoints,
        boolean ownedByMe,
        boolean isFavorited,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CourseDetailResponse from(
            CourseEntity entity,
            List<CourseWaypointResponse> waypoints,
            UUID requestUserId,
            boolean isFavorited
    ) {
        UserEntity author = entity.getUserEntity();
        return new CourseDetailResponse(
                entity.getId(),
                entity.getName(),
                entity.getDistanceMeters(),
                author == null ? "" : author.getNickname(),
                entity.getPath(),
                entity.isPublic(),
                entity.getSourceCourseId(),
                waypoints,
                requestUserId != null && author != null && author.getId().equals(requestUserId),
                isFavorited,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}