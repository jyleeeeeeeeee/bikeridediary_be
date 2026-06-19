package com.bikeridediary.domain.maintenance.dto;

import com.bikeridediary.global.validation.ValidScheduleInterval;
import jakarta.validation.constraints.*;

@ValidScheduleInterval
public record MaintenanceScheduleUpdateRequest(

        @Min(value = 1, message = "km 기준 정비 주기는 1 이상이어야 합니다")
        Long intervalKm,

        @Min(value = 1, message = "개월 기준 정비 주기는 1 이상이어야 합니다")
        Integer intervalMonths
) implements ScheduleIntervalCheckable {
}
