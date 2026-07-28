package com.bikeridediary.domain.fueling.dto;

import com.bikeridediary.domain.fueling.entity.FuelType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FuelingSyncRequest(
        @NotNull UUID id,
        @NotNull UUID bikeId,
        @NotNull LocalDate fuelingDate,
        @NotNull Long mileageAtFueling,
        @NotNull BigDecimal fuelAmount,
        Long pricePerLiter,
        Long totalCost,
        @NotNull FuelType fuelType,
        String memo,
        String stationName,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {}