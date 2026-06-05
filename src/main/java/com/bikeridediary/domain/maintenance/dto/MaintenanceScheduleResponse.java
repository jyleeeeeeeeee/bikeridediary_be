package com.bikeridediary.domain.maintenance.dto;

import com.bikeridediary.domain.maintenance.entity.MaintenanceScheduleEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// 정비 주기 응답 DTO
public record MaintenanceScheduleResponse(
        // 정비 주기 ID
        UUID id,
        // 바이크 ID
        UUID bikeId,
        // 정비 종류
        MaintenanceType maintenanceType,
        // km 기준 정비 주기
        Integer intervalKm,
        // 개월 기준 정비 주기
        Integer intervalMonths,
        // 마지막 정비 시 주행거리 (km)
        Integer lastMaintenanceMileage,
        // 마지막 정비 날짜
        LocalDate lastMaintenanceDate,
        // 정비 필요 여부
        boolean overdue,
        // 등록 일시
        LocalDateTime createdAt,
        // 수정 일시
        LocalDateTime updatedAt
) {

    public static MaintenanceScheduleResponse from(MaintenanceScheduleEntity entity, Integer currentMileage) {
        boolean isOverdue = entity.isOverdue(currentMileage, LocalDate.now());
        return new MaintenanceScheduleResponse(
                entity.getId(),
                entity.getBikeEntity().getId(),
                entity.getMaintenanceType(),
                entity.getIntervalKm(),
                entity.getIntervalMonths(),
                entity.getLastMaintenanceMileage(),
                entity.getLastMaintenanceDate(),
                isOverdue,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
