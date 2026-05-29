package com.bikeridediary.domain.maintenance.service;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.maintenance.dto.MaintenanceCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceUpdateRequest;
import com.bikeridediary.domain.maintenance.entity.MaintenanceEntity;
import com.bikeridediary.domain.maintenance.repository.MaintenanceRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// 정비 기록 비즈니스 로직
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final BikeRepository bikeRepository;

    // 특정 바이크의 모든 정비 기록 조회
    public List<MaintenanceResponse> getMaintenances(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        return maintenanceRepository.findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc(bikeId)
                .stream()
                .map(MaintenanceResponse::from)
                .toList();
    }

    // 특정 정비 기록 조회
    public MaintenanceResponse getMaintenance(UUID maintenanceId, UUID userId) {
        MaintenanceEntity entity = findMaintenanceOrThrow(maintenanceId);
        verifyMaintenanceOwnership(entity, userId);
        return MaintenanceResponse.from(entity);
    }

    // 정비 기록 생성
    @Transactional
    public MaintenanceResponse createMaintenance(MaintenanceCreateRequest request, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(request.bikeId());
        verifyBikeOwnership(bikeEntity, userId);

        MaintenanceEntity entity = MaintenanceEntity.create(
                bikeEntity,
                request.maintenanceType(),
                request.maintenanceDate(),
                request.mileageAtMaintenance(),
                request.cost(),
                request.description(),
                request.nextDueKm(),
                request.nextDueDate()
        );

        MaintenanceEntity saved = maintenanceRepository.save(entity);
        return MaintenanceResponse.from(saved);
    }

    // 정비 기록 수정
    @Transactional
    public MaintenanceResponse updateMaintenance(UUID maintenanceId, MaintenanceUpdateRequest request, UUID userId) {
        MaintenanceEntity entity = findMaintenanceOrThrow(maintenanceId);
        verifyMaintenanceOwnership(entity, userId);

        entity.update(
                request.maintenanceType(),
                request.maintenanceDate(),
                request.mileageAtMaintenance(),
                request.cost(),
                request.description(),
                request.nextDueKm(),
                request.nextDueDate()
        );

        return MaintenanceResponse.from(maintenanceRepository.save(entity));
    }

    // 정비 기록 삭제 (소프트 삭제)
    @Transactional
    public void deleteMaintenance(UUID maintenanceId, UUID userId) {
        MaintenanceEntity entity = findMaintenanceOrThrow(maintenanceId);
        verifyMaintenanceOwnership(entity, userId);

        entity.delete();
        maintenanceRepository.save(entity);
    }

    // ============ 헬퍼 메서드 ============

    private BikeEntity findBikeOrThrow(UUID bikeId) {
        return bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));
    }

    private MaintenanceEntity findMaintenanceOrThrow(UUID maintenanceId) {
        return maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MAINTENANCE_NOT_FOUND));
    }

    private void verifyBikeOwnership(BikeEntity bikeEntity, UUID userId) {
        if (!bikeEntity.isOwner(userId)) {
            throw new BusinessException(ErrorCode.BIKE_ACCESS_DENIED);
        }
    }

    private void verifyMaintenanceOwnership(MaintenanceEntity entity, UUID userId) {
        if (!entity.isOwner(userId)) {
            throw new BusinessException(ErrorCode.MAINTENANCE_ACCESS_DENIED);
        }
    }
}
