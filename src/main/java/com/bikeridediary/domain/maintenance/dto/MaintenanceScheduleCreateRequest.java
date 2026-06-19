package com.bikeridediary.domain.maintenance.dto;

import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import com.bikeridediary.global.validation.ValidScheduleInterval;
import jakarta.validation.constraints.*;

import java.util.UUID;

@ValidScheduleInterval
public record MaintenanceScheduleCreateRequest(

        @NotNull(message = "바이크 ID는 필수입니다")
        UUID bikeId,

        @NotNull(message = "정비 종류는 필수입니다")
        MaintenanceType maintenanceType,

        @Min(value = 1, message = "km 기준 정비 주기는 1 이상이어야 합니다")
        Long intervalKm,

        @Min(value = 1, message = "개월 기준 정비 주기는 1 이상이어야 합니다")
        Integer intervalMonths
) implements ScheduleIntervalCheckable {
}
