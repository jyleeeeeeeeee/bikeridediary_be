package com.bikeridediary.domain.maintenance.dto;

import com.bikeridediary.global.validation.ValidScheduleInterval;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@ValidScheduleInterval
public record MaintenanceScheduleUpdateRequest(

        @Min(value = 1, message = "km 기준 정비 주기는 1 이상이어야 합니다")
        Integer intervalKm,

        @Min(value = 1, message = "개월 기준 정비 주기는 1 이상이어야 합니다")
        Integer intervalMonths,

        @Min(value = 0, message = "마지막 정비 시 주행거리는 0 이상이어야 합니다")
        Integer lastMaintenanceMileage,

        LocalDate lastMaintenanceDate
) implements ScheduleIntervalCheckable {
}
