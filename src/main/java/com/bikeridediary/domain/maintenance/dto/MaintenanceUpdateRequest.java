package com.bikeridediary.domain.maintenance.dto;

import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

// 정비 기록 수정 요청 DTO
public record MaintenanceUpdateRequest(

        @NotNull(message = "정비 종류는 필수입니다")
        MaintenanceType maintenanceType,

        @NotNull(message = "정비 날짜는 필수입니다")
        LocalDate maintenanceDate,

        @NotNull(message = "정비 당시 주행거리는 필수입니다")
        @Min(value = 0, message = "주행거리는 0 이상이어야 합니다")
        Long mileageAtMaintenance,

        @Min(value = 0, message = "비용은 0 이상이어야 합니다")
        Long cost,

        @Size(max = 500, message = "메모는 500자 이하여야 합니다")
        String description,

        @Min(value = 0, message = "다음 정비 예정 주행거리는 0 이상이어야 합니다")
        Long nextDueKm,

        LocalDate nextDueDate,
        List<String> existingImageUrls
) {
}
