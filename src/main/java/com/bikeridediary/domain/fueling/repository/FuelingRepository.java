package com.bikeridediary.domain.fueling.repository;

import com.bikeridediary.domain.fueling.entity.FuelingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelingRepository extends JpaRepository<FuelingEntity, UUID> {

    // 특정 주유 기록 조회 (소프트 삭제 제외)
    Optional<FuelingEntity> findByIdAndDeletedAtIsNull(UUID id);

    // 특정 바이크의 모든 주유 기록 조회 (최신순)
    List<FuelingEntity> findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDescMileageAtFuelingDesc(UUID bikeId);

    // 특정 바이크의 특정 주행거리 이전의 가장 최근 만탱크 기록 조회 (연비 계산용)
    Optional<FuelingEntity> findTopByBikeEntityIdAndIsFullTankTrueAndMileageAtFuelingLessThanAndDeletedAtIsNullOrderByMileageAtFuelingDesc(
            UUID bikeId, Integer mileageAtFueling);

    // 특정 바이크의 두 주행거리 사이 주유 기록들의 총 주유량 합산 (연비 계산용)
    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(f.fuelAmount), 0) FROM FuelingEntity f " +
            "WHERE f.bikeEntity.id = :bikeId " +
            "AND f.mileageAtFueling > :fromMileage " +
            "AND f.mileageAtFueling <= :toMileage " +
            "AND f.deletedAt IS NULL")
    java.math.BigDecimal sumFuelAmountBetweenMileage(UUID bikeId, Integer fromMileage, Integer toMileage);
}
