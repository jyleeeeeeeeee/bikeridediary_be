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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuelingService {

    private final FuelingRepository fuelingRepository;
    private final BikeRepository bikeRepository;

    public List<FuelingResponse> getFuelings(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        return fuelingRepository
                .findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDescMileageAtFuelingDesc(bikeId)
                .stream()
                .map(FuelingResponse::from)
                .toList();
    }

    public FuelingResponse getFueling(UUID fuelingId, UUID userId) {
        FuelingEntity entity = findFuelingOrThrow(fuelingId);
        verifyFuelingOwnership(entity, userId);
        return FuelingResponse.from(entity);
    }

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
                request.memo(),
                request.stationName()
        );

        BigDecimal efficiency = calculateFuelEfficiency(
                request.bikeId(), request.mileageAtFueling(), request.fuelAmount());
        entity.setFuelEfficiency(efficiency);

        FuelingEntity saved = fuelingRepository.save(entity);
        return FuelingResponse.from(saved);
    }

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
                request.memo(),
                request.stationName()
        );

        BigDecimal efficiency = calculateFuelEfficiency(
                entity.getBikeEntity().getId(), request.mileageAtFueling(), request.fuelAmount());
        entity.setFuelEfficiency(efficiency);

        return FuelingResponse.from(entity);
    }

    @Transactional
    public void deleteFueling(UUID fuelingId, UUID userId) {
        FuelingEntity entity = findFuelingOrThrow(fuelingId);
        verifyFuelingOwnership(entity, userId);
        entity.delete();
    }

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

        BigDecimal latestEfficiency = efficiencies.isEmpty() ? null : efficiencies.getFirst();

        List<Long> prices = fuelings.stream()
                .filter(f -> f.getPricePerLiter() != null)
                .map(FuelingEntity::getPricePerLiter)
                .toList();
        Double avgPrice = prices.isEmpty() ? null :
                prices.stream().mapToLong(Long::longValue).average().orElse(0);

        return new FuelingStatsResponse(
                fuelings.size(), totalFuel, totalCost,
                avgEfficiency, latestEfficiency, avgPrice
        );
    }

    // 연비 계산: (현재 주행거리 - 이전 주유 시 주행거리) / 현재 주유량
    private BigDecimal calculateFuelEfficiency(UUID bikeId, Long currentMileage, BigDecimal currentFuelAmount) {
        var prev = fuelingRepository
                .findTopByBikeEntityIdAndMileageAtFuelingLessThanAndDeletedAtIsNullOrderByMileageAtFuelingDesc(
                        bikeId, currentMileage);

        if (prev.isEmpty()) {
            return null;
        }

        long distanceKm = currentMileage - prev.get().getMileageAtFueling();
        if (distanceKm <= 0) {
            return null;
        }

        if (currentFuelAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return BigDecimal.valueOf(distanceKm)
                .divide(currentFuelAmount, 2, RoundingMode.HALF_UP);
    }

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
