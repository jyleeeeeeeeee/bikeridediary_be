package com.bikeridediary.domain.place.dto;

import com.bikeridediary.domain.place.entity.PlaceEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceResponse(
        UUID id,
        UUID userId,          // 등록자 UUID (nullable — 시드/큐레이션 장소는 없음)
        String placeName,
        String category,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String roadAddress,
        String description,
        String photoUrl,
        String phone,
        String kakaoPlaceId,
        String naverPlaceId
) {
    public static PlaceResponse from(PlaceEntity entity) {
        return new PlaceResponse(
                entity.getId(),
                entity.getUserEntity() == null ? null : entity.getUserEntity().getId(),
                entity.getPlaceName(),
                entity.getPlaceCategoryEntity().getCategoryCode(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getAddress(),
                entity.getRoadAddress(),
                entity.getDescription(),
                entity.getPhotoUrl(),
                entity.getPhone(),
                entity.getKakaoPlaceId(),
                entity.getNaverPlaceId()
        );
    }
}
