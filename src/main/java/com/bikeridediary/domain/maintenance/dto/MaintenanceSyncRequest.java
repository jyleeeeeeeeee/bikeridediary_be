package com.bikeridediary.domain.maintenance.dto;

import com.bikeridediary.domain.fueling.entity.FuelType;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MaintenanceSyncRequest(
        @NotNull UUID id,
        @NotNull UUID bikeId,
        @NotNull MaintenanceType maintenanceType,
        @NotNull LocalDate maintenanceDate,
        @NotNull Long mileageAtMaintenance,
        Long cost,
        String description,
        Long nextDueKm,
        LocalDate nextDueDate,
        /// 이미 서버에 저장된 URL 목록. 여기서 빠진 URL은 삭제 대상.
        List<String> existingImageUrls,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {}