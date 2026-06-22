package com.bikeridediary.domain.maintenance.dto;

import com.bikeridediary.domain.maintenance.entity.MaintenanceEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// 정비 기록 응답 DTO
public record MaintenanceResponse(
        // 정비 기록 ID
        UUID id,
        // 바이크 ID
        UUID bikeId,
        // 정비 종류
        MaintenanceType maintenanceType,
        // 정비 날짜
        LocalDate maintenanceDate,
        // 정비 당시 주행거리 (km)
        Long mileageAtMaintenance,
        // 비용 (원)
        Long cost,
        // 메모
        String description,
        // 다음 정비 예정 주행거리 (km)
        Long nextDueKm,
        // 다음 정비 예정 날짜
        LocalDate nextDueDate,
        // 등록 일시
        LocalDateTime createdAt,
        // 수정 일시
        LocalDateTime updatedAt
) {

    public static MaintenanceResponse from(MaintenanceEntity entity) {
        return new MaintenanceResponse(
                entity.getId(),
                entity.getBikeEntity().getId(),
                entity.getMaintenanceType(),
                entity.getMaintenanceDate(),
                entity.getMileageAtMaintenance(),
                entity.getCost(),
                entity.getDescription(),
                entity.getNextDueKm(),
                entity.getNextDueDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
