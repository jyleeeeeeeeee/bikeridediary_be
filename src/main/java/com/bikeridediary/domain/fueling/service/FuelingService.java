package com.bikeridediary.domain.fueling.service;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.fueling.dto.*;
import com.bikeridediary.domain.fueling.entity.FuelingEntity;
import com.bikeridediary.domain.fueling.repository.FuelingRepository;
import com.bikeridediary.domain.maintenance.repository.MaintenanceRepository;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import com.bikeridediary.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.bikeridediary.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuelingService {

    private final FuelingRepository fuelingRepository;
    private final BikeRepository bikeRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final UserRepository userRepository;

    // 사용자 화면 목록 (페이징) — fuelingDate DESC, 동일 날짜는 mileageAtFueling DESC
    public PageResponse<FuelingResponse> getFuelings(UUID bikeId, UUID userId, Pageable pageable) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        return PageResponse.of(
                fuelingRepository.findByBikeEntityIdAndDeletedAtIsNull(bikeId, pageable),
                FuelingResponse::from
        );
    }

    // 통계 계산용 내부 조회 — 전체 데이터 필요 (fuelings 컬렉션 순회로 sum/avg 계산)
    private List<FuelingEntity> loadAllFuelings(UUID bikeId) {
        return fuelingRepository
                .findByBikeEntityIdAndDeletedAtIsNull(bikeId, Pageable.unpaged(
                        Sort.by(Sort.Direction.DESC, "fuelingDate", "mileageAtFueling")))
                .getContent();
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
        updateBikeMileage(bikeEntity);
        updateBikeFuelEfficiency(bikeEntity);
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

        updateBikeMileage(entity.getBikeEntity());
        updateBikeFuelEfficiency(entity.getBikeEntity());
        return FuelingResponse.from(entity);
    }

    @Transactional
    public void deleteFueling(UUID fuelingId, UUID userId) {
        FuelingEntity entity = findFuelingOrThrow(fuelingId);
        verifyFuelingOwnership(entity, userId);
        BikeEntity bikeEntity = entity.getBikeEntity();
        entity.delete();
        updateBikeMileage(bikeEntity);
        updateBikeFuelEfficiency(bikeEntity);
    }

    public FuelingStatsResponse getStats(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        List<FuelingEntity> fuelings = loadAllFuelings(bikeId);

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

    private void updateBikeMileage(BikeEntity bikeEntity) {
        Long maxFueling = fuelingRepository.findMaxMileageByBikeId(bikeEntity.getId());
        Long maxMaintenance = maintenanceRepository.findMaxMileageByBikeId(bikeEntity.getId());
        long maxMileage = Math.max(
                bikeEntity.getTotalMileageKm(),
                Math.max(
                        maxFueling != null ? maxFueling : 0L,
                        maxMaintenance != null ? maxMaintenance : 0L
                )
        );
        bikeEntity.setTotalMileageKm(maxMileage);
    }

    private void updateBikeFuelEfficiency(BikeEntity bikeEntity) {
        List<FuelingEntity> fuelings = loadAllFuelings(bikeEntity.getId());

        List<BigDecimal> efficiencies = fuelings.stream()
                .filter(f -> f.getFuelEfficiency() != null)
                .map(FuelingEntity::getFuelEfficiency)
                .toList();

        BigDecimal latest = efficiencies.isEmpty() ? null : efficiencies.get(0);
        BigDecimal average = efficiencies.isEmpty() ? null :
                efficiencies.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(efficiencies.size()), 2, RoundingMode.HALF_UP);

        bikeEntity.updateFuelEfficiency(latest, average);
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
                .orElseThrow(() -> new BusinessException(BIKE_NOT_FOUND));
    }

    private FuelingEntity findFuelingOrThrow(UUID fuelingId) {
        return fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId)
                .orElseThrow(() -> new BusinessException(FUELING_NOT_FOUND));
    }

    private void verifyBikeOwnership(BikeEntity bikeEntity, UUID userId) {
        if (!bikeEntity.isOwner(userId)) {
            throw new BusinessException(BIKE_ACCESS_DENIED);
        }
    }

    private void verifyFuelingOwnership(FuelingEntity entity, UUID userId) {
        if (!entity.isOwner(userId)) {
            throw new BusinessException(FUELING_ACCESS_DENIED);
        }
    }

    @Transactional
    public FuelingResponse sync(UUID userId, FuelingSyncRequest request) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        BikeEntity bike = bikeRepository.findByIdAndDeletedAtIsNull(request.bikeId())
                .orElseThrow(() -> new BusinessException(BIKE_NOT_FOUND));
        if(!bike.isOwner(userId)) throw new BusinessException(BIKE_ACCESS_DENIED);

        FuelingEntity target;
        Optional<FuelingEntity> existingOpt = fuelingRepository.findByIdAndDeletedAtIsNull(request.id());
        if(existingOpt.isPresent()) {
            FuelingEntity existing = existingOpt.get();
            if(!existing.isOwner(userId)) throw new BusinessException(FUELING_ACCESS_DENIED);
            if(request.deletedAt() != null && !existing.isDeleted()) return FuelingResponse.from(existing);

            if(request.updatedAt().isAfter(existing.getUpdatedAt())) {
                existing.update(request.fuelingDate(), request.mileageAtFueling(), request.fuelAmount(),
                        request.pricePerLiter(), request.totalCost(), request.fuelType(), request.memo(), request.stationName());
                target = existing;
            } else {
                return FuelingResponse.from(existing);
            }
        } else {
            FuelingEntity toSave = FuelingEntity.createWithId(
                    request.id(), bike,
                    request.fuelingDate(), request.mileageAtFueling(), request.fuelAmount(),
                    request.pricePerLiter(), request.totalCost(), request.fuelType(),
                    request.memo(), request.stationName());
            // save 반환값이 managed 엔티티. ID 수동 세팅 → Spring이 merge 사용 →
            // @PrePersist는 managed 복사본에만 발생하므로 반환값을 사용해야 createdAt이 채워짐.
            target = fuelingRepository.save(toSave);
        }

        // efficiency 재계산 — 이전 주유 기록 기준
        recalculateFuelEfficiency(target);
        // 바이크 total_mileage_km 갱신 (기존 create/update 흐름과 동일)
        updateBikeMileage(bike);
        return FuelingResponse.from(target);
    }

    private void recalculateFuelEfficiency(FuelingEntity current) {
        // 같은 바이크의 이 시점 이전(fuelingDate/mileage) 가장 최근 활성 기록 조회
        Optional<FuelingEntity> prevOpt = fuelingRepository
                .findTopByBikeEntityAndFuelingDateBeforeAndDeletedAtIsNullOrderByFuelingDateDesc(
                        current.getBikeEntity(), current.getFuelingDate());
        if (prevOpt.isEmpty()) {
            current.setFuelEfficiency(null);
            return;
        }
        long deltaKm = current.getMileageAtFueling() - prevOpt.get().getMileageAtFueling();
        if (deltaKm <= 0 || current.getFuelAmount().signum() <= 0) {
            current.setFuelEfficiency(null);
            return;
        }
        BigDecimal efficiency = BigDecimal.valueOf(deltaKm)
                .divide(current.getFuelAmount(), 2, RoundingMode.HALF_UP);
        current.setFuelEfficiency(efficiency);
    }

    public List<FuelingResponse> getMyFuelings(UUID userId) {
        return fuelingRepository.findByBikeEntity_UserEntity_IdAndDeletedAtIsNullOrderByFuelingDateDesc(userId)
                .stream().map(FuelingResponse::from).toList();
    }
}
