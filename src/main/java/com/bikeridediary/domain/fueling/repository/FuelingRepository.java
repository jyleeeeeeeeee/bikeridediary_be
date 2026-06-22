package com.bikeridediary.domain.fueling.repository;

import com.bikeridediary.domain.fueling.entity.FuelingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelingRepository extends JpaRepository<FuelingEntity, UUID> {

    Optional<FuelingEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<FuelingEntity> findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDescMileageAtFuelingDesc(UUID bikeId);

    // 현재 주행거리 직전의 주유 기록 조회 (연비 계산용)
    Optional<FuelingEntity> findTopByBikeEntityIdAndMileageAtFuelingLessThanAndDeletedAtIsNullOrderByMileageAtFuelingDesc(
            UUID bikeId, Long mileageAtFueling);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(f.fuelAmount), 0) FROM FuelingEntity f " +
            "WHERE f.bikeEntity.id = :bikeId " +
            "AND f.mileageAtFueling > :fromMileage " +
            "AND f.mileageAtFueling <= :toMileage " +
            "AND f.deletedAt IS NULL")
    java.math.BigDecimal sumFuelAmountBetweenMileage(UUID bikeId, Long fromMileage, Long toMileage);
}
