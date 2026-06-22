package com.bikeridediary.domain.fueling.dto;

import com.bikeridediary.domain.fueling.entity.FuelType;
import com.bikeridediary.domain.fueling.entity.FuelingEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FuelingResponse(
        UUID id,
        UUID bikeId,
        LocalDate fuelingDate,
        Long mileageAtFueling,
        BigDecimal fuelAmount,
        Long pricePerLiter,
        Long totalCost,
        FuelType fuelType,
        BigDecimal fuelEfficiency,
        String memo,
        String stationName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static FuelingResponse from(FuelingEntity entity) {
        return new FuelingResponse(
                entity.getId(),
                entity.getBikeEntity().getId(),
                entity.getFuelingDate(),
                entity.getMileageAtFueling(),
                entity.getFuelAmount(),
                entity.getPricePerLiter(),
                entity.getTotalCost(),
                entity.getFuelType(),
                entity.getFuelEfficiency(),
                entity.getMemo(),
                entity.getStationName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
