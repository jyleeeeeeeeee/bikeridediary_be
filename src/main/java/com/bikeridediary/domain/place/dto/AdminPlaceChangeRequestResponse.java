package com.bikeridediary.domain.place.dto;

import com.bikeridediary.domain.place.entity.PlaceChangeRequestEntity;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestStatus;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// 어드민 목록/상세용 응답 (요청자 정보 포함)
public record AdminPlaceChangeRequestResponse(
        UUID id,
        PlaceChangeRequestType type,
        UUID targetPlaceId,
        String targetPlaceName,
        UUID requesterId,
        String requesterNickname,
        Map<String, Object> payload,
        PlaceChangeRequestStatus status,
        String reviewNote,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {
    public static AdminPlaceChangeRequestResponse from(PlaceChangeRequestEntity e) {
        return new AdminPlaceChangeRequestResponse(
                e.getId(),
                e.getType(),
                e.getTargetPlace() != null ? e.getTargetPlace().getId() : null,
                e.getTargetPlace() != null ? e.getTargetPlace().getPlaceName() : null,
                e.getRequester().getId(),
                e.getRequester().getNickname(),
                e.getPayload(),
                e.getStatus(),
                e.getReviewNote(),
                e.getReviewedAt(),
                e.getCreatedAt()
        );
    }
}