package com.bikeridediary.domain.maintenance.dto;

import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceScheduleSyncRequest(
        @NotNull UUID id,
        @NotNull UUID bikeId,
        @NotNull MaintenanceType maintenanceType,
        Long intervalKm,
        // 엔티티는 Integer. 앱 sync JSON은 정수 그대로 오므로 문제 없음.
        Integer intervalMonths,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {}
