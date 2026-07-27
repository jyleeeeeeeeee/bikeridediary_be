package com.bikeridediary.domain.maintenance.repository;

import com.bikeridediary.domain.maintenance.entity.MaintenanceEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 정비 기록 데이터 접근 인터페이스
public interface MaintenanceRepository extends JpaRepository<MaintenanceEntity, UUID> {

    // 특정 바이크의 활성 정비 기록 (페이징, maintenanceDate DESC 정렬은 Pageable Sort로)
    Page<MaintenanceEntity> findByBikeEntityIdAndDeletedAtIsNull(UUID bikeId, Pageable pageable);

    // 특정 활성 정비 기록 조회
    Optional<MaintenanceEntity> findByIdAndDeletedAtIsNull(UUID id);

    // 특정 바이크의 특정 정비 종류 기록 조회 (최신순)
    List<MaintenanceEntity> findByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMaintenanceDateDesc(
            UUID bikeId, MaintenanceType maintenanceType);

    // 특정 바이크의 특정 정비 종류 최신 기록 1건 조회 (날짜 기준)
    Optional<MaintenanceEntity> findTopByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMaintenanceDateDesc(
            UUID bikeId, MaintenanceType maintenanceType);

    // 특정 바이크의 특정 정비 종류 최신 기록 1건 조회 (주행거리 기준)
    Optional<MaintenanceEntity> findTopByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMileageAtMaintenanceDesc(
            UUID bikeId, MaintenanceType maintenanceType);

    @org.springframework.data.jpa.repository.Query(
            "SELECT MAX(m.mileageAtMaintenance) FROM MaintenanceEntity m " +
            "WHERE m.bikeEntity.id = :bikeId AND m.deletedAt IS NULL")
    Long findMaxMileageByBikeId(UUID bikeId);

    // no(자동 증가 조회 번호)로 특정 maintenance 조회 — DB 관리/디버깅용
    Optional<MaintenanceEntity> findByNo(Long no);
}
