package com.bikeridediary.domain.bike.dto;

import com.bikeridediary.domain.bike.entity.BikeCategory;
import jakarta.validation.constraints.*;

// 새 바이크 생성 요청 DTO
public record BikeCreateRequest(
        @NotBlank(message = "제조사명은 필수입니다")
        @Size(max = 100, message = "제조사명은 100자 이하여야 합니다")
        String manufacturerName,

        @NotBlank(message = "모델명은 필수입니다")
        @Size(max = 100, message = "모델명은 100자 이하여야 합니다")
        String modelName,

        @NotNull(message = "연식은 필수입니다")
        @Min(value = 1900, message = "연식은 1900 이상이어야 합니다")
        @Max(value = 2100, message = "연식은 2100 이하여야 합니다")
        Integer year,

        @NotNull(message = "카테고리는 필수입니다")
        BikeCategory category,

        @NotNull(message = "총 주행거리는 필수입니다")
        @Min(value = 0, message = "총 주행거리는 0 이상이어야 합니다")
        Integer totalMileageKm
) {
}
