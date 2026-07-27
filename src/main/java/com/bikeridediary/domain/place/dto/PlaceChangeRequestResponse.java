package com.bikeridediary.domain.place.dto;

import com.bikeridediary.domain.place.entity.PlaceChangeRequestEntity;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestStatus;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// 요청자 본인용 응답 (내 요청 목록 / 상세)
public record PlaceChangeRequestResponse(
        UUID id,
        PlaceChangeRequestType type,
        UUID targetPlaceId,
        String targetPlaceName,     // UPDATE_*일 때만 값 (null 가능)
        Map<String, Object> payload,
        PlaceChangeRequestStatus status,
        String reviewNote,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {
    public static PlaceChangeRequestResponse from(PlaceChangeRequestEntity e) {
        return new PlaceChangeRequestResponse(
                e.getId(),
                e.getType(),
                e.getTargetPlace() != null ? e.getTargetPlace().getId() : null,
                e.getTargetPlace() != null ? e.getTargetPlace().getPlaceName() : null,
                e.getPayload(),
                e.getStatus(),
                e.getReviewNote(),
                e.getReviewedAt(),
                e.getCreatedAt()
        );
    }
}