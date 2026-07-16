package com.bikeridediary.domain.course.dto;

import com.bikeridediary.domain.course.entity.CourseWaypointEntity;
import com.bikeridediary.domain.place.entity.PlaceEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseWaypointResponse(
        UUID id,
        short seq,
        String role,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        // 참조된 place ID (임의 지점은 null)
        UUID placeId,
        // 참조된 place의 카테고리 코드 (앱에서 마커 아이콘 결정용, null 가능)
        String placeCategoryCode
) {
    public static CourseWaypointResponse from(CourseWaypointEntity entity) {
        PlaceEntity placeEntity = entity.getPlaceEntity();
        return new CourseWaypointResponse(
                entity.getId(),
                entity.getSeq(),
                entity.getRole(),
                entity.getName(),
                entity.getLatitude(),
                entity.getLongitude(),
                placeEntity == null ? null : placeEntity.getId(),
                placeEntity == null ? null : placeEntity.getPlaceCategoryEntity().getCategoryCode()
        );
    }
}
