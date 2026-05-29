package com.bikeridediary.domain.maintenance.repository;

import com.bikeridediary.domain.maintenance.entity.MaintenanceEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 정비 기록 데이터 접근 인터페이스
public interface MaintenanceRepository extends JpaRepository<MaintenanceEntity, UUID> {

    // 특정 바이크의 모든 활성 정비 기록 조회 (최신순)
    List<MaintenanceEntity> findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc(UUID bikeId);

    // 특정 활성 정비 기록 조회
    Optional<MaintenanceEntity> findByIdAndDeletedAtIsNull(UUID id);

    // 특정 바이크의 특정 정비 종류 기록 조회 (최신순)
    List<MaintenanceEntity> findByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMaintenanceDateDesc(
            UUID bikeId, MaintenanceType maintenanceType);
}
