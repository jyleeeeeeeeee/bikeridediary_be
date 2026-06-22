package com.bikeridediary.domain.fueling.dto;

import com.bikeridediary.domain.fueling.entity.FuelType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FuelingCreateRequest(

        @NotNull(message = "바이크 ID는 필수입니다")
        UUID bikeId,

        @NotNull(message = "주유 날짜는 필수입니다")
        LocalDate fuelingDate,

        @NotNull(message = "주유 시 주행거리는 필수입니다")
        @Min(value = 0, message = "주행거리는 0 이상이어야 합니다")
        Long mileageAtFueling,

        @NotNull(message = "주유량은 필수입니다")
        @DecimalMin(value = "0.01", message = "주유량은 0.01 이상이어야 합니다")
        BigDecimal fuelAmount,

        @Min(value = 0, message = "리터당 가격은 0 이상이어야 합니다")
        Long pricePerLiter,

        @Min(value = 0, message = "총 비용은 0 이상이어야 합니다")
        Long totalCost,

        @NotNull(message = "연료 종류는 필수입니다")
        FuelType fuelType,

        @Size(max = 500, message = "메모는 500자 이하여야 합니다")
        String memo,

        @Size(max = 100, message = "주유소명은 100자 이하여야 합니다")
        String stationName
) {
}
