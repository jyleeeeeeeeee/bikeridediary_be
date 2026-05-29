package com.bikeridediary.domain.bike.dto;

import com.bikeridediary.domain.bike.entity.BikeCategory;
import com.bikeridediary.domain.bike.entity.BikeEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// 바이크 정보 응답 DTO
public record BikeResponse(
        UUID id,
        String manufacturerName,
        String modelName,
        Integer year,
        BikeCategory category,
        Integer totalMileageKm,
        boolean isRepresentative,
        LocalDate purchasedAt,
        String photoUrl,
        String memo,
        LocalDateTime createdAt
) {

    public static BikeResponse from(BikeEntity bikeEntity) {
        return new BikeResponse(
                bikeEntity.getId(),
                bikeEntity.getManufacturerName(),
                bikeEntity.getModelName(),
                bikeEntity.getYear(),
                bikeEntity.getCategory(),
                bikeEntity.getTotalMileageKm(),
                bikeEntity.isRepresentative(),
                bikeEntity.getPurchasedAt(),
                bikeEntity.getPhotoUrl(),
                bikeEntity.getMemo(),
                bikeEntity.getCreatedAt()
        );
    }
}
