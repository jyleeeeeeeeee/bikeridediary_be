package com.bikeridediary.domain.fueling.dto;

import java.math.BigDecimal;

// 주유 통계 응답 DTO - 특정 바이크의 연비/비용 요약
public record FuelingStatsResponse(
        // 총 주유 횟수
        int totalCount,
        // 총 주유량 (리터)
        BigDecimal totalFuelAmount,
        // 총 주유 비용 (원)
        long totalCost,
        // 평균 연비 (km/L — 연비 데이터가 있는 기록들의 평균)
        BigDecimal averageFuelEfficiency,
        // 최근 연비 (km/L)
        BigDecimal latestFuelEfficiency,
        // 평균 리터당 가격 (원)
        Integer averagePricePerLiter
) {
}
