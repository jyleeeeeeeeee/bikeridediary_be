package com.bikeridediary.domain.fueling.repository;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.fueling.entity.FuelingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelingRepository extends JpaRepository<FuelingEntity, UUID> {

    Optional<FuelingEntity> findByIdAndDeletedAtIsNull(UUID id);

    // 주유 이력 페이징 (fuelingDate DESC, mileageAtFueling DESC 정렬은 Pageable Sort로)
    Page<FuelingEntity> findByBikeEntityIdAndDeletedAtIsNull(UUID bikeId, Pageable pageable);

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

    @org.springframework.data.jpa.repository.Query(
            "SELECT MAX(f.mileageAtFueling) FROM FuelingEntity f " +
                    "WHERE f.bikeEntity.id = :bikeId AND f.deletedAt IS NULL")
    Long findMaxMileageByBikeId(UUID bikeId);

    // no(자동 증가 조회 번호)로 특정 fueling 조회 — DB 관리/디버깅용
    Optional<FuelingEntity> findByNo(Long no);

    // 특정 바이크의 fuelingDate 이전 가장 최근 기록 (efficiency 재계산용)
    Optional<FuelingEntity> findTopByBikeEntityAndFuelingDateBeforeAndDeletedAtIsNullOrderByFuelingDateDesc(
            BikeEntity bikeEntity, LocalDate fuelingDate);

    // 유저의 모든 활성 주유 기록 (getMyFuelings용)
    @Query("SELECT f FROM FuelingEntity f " +
            "JOIN FETCH f.bikeEntity b " +
            "WHERE b.userEntity.id = :userId AND f.deletedAt IS NULL " +
            "ORDER BY f.fuelingDate DESC")
    List<FuelingEntity> findByBikeEntity_UserEntity_IdAndDeletedAtIsNullOrderByFuelingDateDesc(UUID userId);
}

