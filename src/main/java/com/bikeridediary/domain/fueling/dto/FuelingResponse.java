package com.bikeridediary.domain.fueling.dto;

import com.bikeridediary.domain.fueling.entity.FuelType;
import com.bikeridediary.domain.fueling.entity.FuelingEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// 주유 기록 응답 DTO
public record FuelingResponse(
        // 주유 기록 ID
        UUID id,
        // 바이크 ID
        UUID bikeId,
        // 주유 날짜
        LocalDate fuelingDate,
        // 주유 시 주행거리 (km)
        Integer mileageAtFueling,
        // 주유량 (리터)
        BigDecimal fuelAmount,
        // 리터당 가격 (원)
        Integer pricePerLiter,
        // 총 주유 비용 (원)
        Integer totalCost,
        // 연료 종류
        FuelType fuelType,
        // 만탱크 여부
        boolean isFullTank,
        // 연비 (km/L, 만탱크법 기반 계산 — null이면 계산 불가)
        BigDecimal fuelEfficiency,
        // 메모
        String memo,
        // 주유소명
        String stationName,
        // 등록 일시
        LocalDateTime createdAt,
        // 수정 일시
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
                entity.isFullTank(),
                entity.getFuelEfficiency(),
                entity.getMemo(),
                entity.getStationName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
