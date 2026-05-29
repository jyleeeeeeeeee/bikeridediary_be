package com.bikeridediary.domain.maintenance.repository;

import com.bikeridediary.domain.maintenance.entity.MaintenanceScheduleEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 정비 주기 데이터 접근 인터페이스
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceScheduleEntity, UUID> {

    // 특정 바이크의 모든 활성 정비 주기 조회
    List<MaintenanceScheduleEntity> findByBikeEntityIdAndDeletedAtIsNull(UUID bikeId);

    // 특정 활성 정비 주기 조회
    Optional<MaintenanceScheduleEntity> findByIdAndDeletedAtIsNull(UUID id);

    // 특정 바이크의 특정 정비 종류 주기 조회
    Optional<MaintenanceScheduleEntity> findByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull(
            UUID bikeId, MaintenanceType maintenanceType);

    // 특정 바이크에 동일한 정비 종류의 주기가 존재하는지 확인
    boolean existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull(UUID bikeId, MaintenanceType maintenanceType);
}
