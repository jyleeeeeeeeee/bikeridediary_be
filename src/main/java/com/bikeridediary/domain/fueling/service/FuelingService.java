package com.bikeridediary.domain.fueling.service;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.fueling.dto.*;
import com.bikeridediary.domain.fueling.entity.FuelingEntity;
import com.bikeridediary.domain.fueling.repository.FuelingRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

// 주유 기록 비즈니스 로직 (만탱크법 기반 연비 계산 포함)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuelingService {

    private final FuelingRepository fuelingRepository;
    private final BikeRepository bikeRepository;

    // 특정 바이크의 모든 주유 기록 조회
    public List<FuelingResponse> getFuelings(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        return fuelingRepository
                .findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDescMileageAtFuelingDesc(bikeId)
                .stream()
                .map(FuelingResponse::from)
                .toList();
    }

    // 특정 주유 기록 조회
    public FuelingResponse getFueling(UUID fuelingId, UUID userId) {
        FuelingEntity entity = findFuelingOrThrow(fuelingId);
        verifyFuelingOwnership(entity, userId);
        return FuelingResponse.from(entity);
    }

    // 주유 기록 생성 + 만탱크법 연비 계산
    @Transactional
    public FuelingResponse createFueling(FuelingCreateRequest request, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(request.bikeId());
        verifyBikeOwnership(bikeEntity, userId);

        FuelingEntity entity = FuelingEntity.create(
                bikeEntity,
                request.fuelingDate(),
                request.mileageAtFueling(),
                request.fuelAmount(),
                request.pricePerLiter(),
                request.totalCost(),
                request.fuelType(),
                request.isFullTank(),
                request.memo(),
                request.stationName()
        );

        if (request.isFullTank()) {
            BigDecimal efficiency = calculateFuelEfficiency(
                    request.bikeId(), request.mileageAtFueling());
            entity.setFuelEfficiency(efficiency);
        }

        FuelingEntity saved = fuelingRepository.save(entity);
        return FuelingResponse.from(saved);
    }

    // 주유 기록 수정 + 연비 재계산
    @Transactional
    public FuelingResponse updateFueling(UUID fuelingId, FuelingUpdateRequest request, UUID userId) {
        FuelingEntity entity = findFuelingOrThrow(fuelingId);
        verifyFuelingOwnership(entity, userId);

        entity.update(
                request.fuelingDate(),
                request.mileageAtFueling(),
                request.fuelAmount(),
                request.pricePerLiter(),
                request.totalCost(),
                request.fuelType(),
                request.isFullTank(),
                request.memo(),
                request.stationName()
        );

        if (request.isFullTank()) {
            BigDecimal efficiency = calculateFuelEfficiency(
                    entity.getBikeEntity().getId(), request.mileageAtFueling());
            entity.setFuelEfficiency(efficiency);
        } else {
            entity.setFuelEfficiency(null);
        }

        return FuelingResponse.from(entity);
    }

    // 주유 기록 삭제 (소프트 삭제)
    @Transactional
    public void deleteFueling(UUID fuelingId, UUID userId) {
        FuelingEntity entity = findFuelingOrThrow(fuelingId);
        verifyFuelingOwnership(entity, userId);
        entity.delete();
    }

    // 특정 바이크의 주유 통계 조회
    public FuelingStatsResponse getStats(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        List<FuelingEntity> fuelings = fuelingRepository
                .findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDescMileageAtFuelingDesc(bikeId);

        if (fuelings.isEmpty()) {
            return new FuelingStatsResponse(0, BigDecimal.ZERO, 0L, null, null, null);
        }

        BigDecimal totalFuel = fuelings.stream()
                .map(FuelingEntity::getFuelAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalCost = fuelings.stream()
                .filter(f -> f.getTotalCost() != null)
                .mapToLong(FuelingEntity::getTotalCost)
                .sum();

        List<BigDecimal> efficiencies = fuelings.stream()
                .filter(f -> f.getFuelEfficiency() != null)
                .map(FuelingEntity::getFuelEfficiency)
                .toList();

        BigDecimal avgEfficiency = efficiencies.isEmpty() ? null :
                efficiencies.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(efficiencies.size()), 2, RoundingMode.HALF_UP);

        BigDecimal latestEfficiency = efficiencies.isEmpty() ? null : efficiencies.get(0);

        List<Integer> prices = fuelings.stream()
                .filter(f -> f.getPricePerLiter() != null)
                .map(FuelingEntity::getPricePerLiter)
                .toList();
        Integer avgPrice = prices.isEmpty() ? null :
                (int) prices.stream().mapToInt(Integer::intValue).average().orElse(0);

        return new FuelingStatsResponse(
                fuelings.size(), totalFuel, totalCost,
                avgEfficiency, latestEfficiency, avgPrice
        );
    }

    // ============ 만탱크법 연비 계산 ============

    // 이전 만탱크 시점부터 현재 만탱크까지의 주행거리 / 누적 주유량
    private BigDecimal calculateFuelEfficiency(UUID bikeId, Integer currentMileage) {
        var prevFullTank = fuelingRepository
                .findTopByBikeEntityIdAndIsFullTankTrueAndMileageAtFuelingLessThanAndDeletedAtIsNullOrderByMileageAtFuelingDesc(
                        bikeId, currentMileage);

        if (prevFullTank.isEmpty()) {
            return null;
        }

        Integer prevMileage = prevFullTank.get().getMileageAtFueling();
        int distanceKm = currentMileage - prevMileage;
        if (distanceKm <= 0) {
            return null;
        }

        BigDecimal totalFuel = fuelingRepository.sumFuelAmountBetweenMileage(
                bikeId, prevMileage, currentMileage);

        if (totalFuel.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return BigDecimal.valueOf(distanceKm)
                .divide(totalFuel, 2, RoundingMode.HALF_UP);
    }

    // ============ 헬퍼 메서드 ============

    private BikeEntity findBikeOrThrow(UUID bikeId) {
        return bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));
    }

    private FuelingEntity findFuelingOrThrow(UUID fuelingId) {
        return fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FUELING_NOT_FOUND));
    }

    private void verifyBikeOwnership(BikeEntity bikeEntity, UUID userId) {
        if (!bikeEntity.isOwner(userId)) {
            throw new BusinessException(ErrorCode.BIKE_ACCESS_DENIED);
        }
    }

    private void verifyFuelingOwnership(FuelingEntity entity, UUID userId) {
        if (!entity.isOwner(userId)) {
            throw new BusinessException(ErrorCode.FUELING_ACCESS_DENIED);
        }
    }
}
