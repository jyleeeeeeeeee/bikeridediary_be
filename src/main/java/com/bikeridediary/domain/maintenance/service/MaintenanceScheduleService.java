package com.bikeridediary.domain.maintenance.service;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleUpdateRequest;
import com.bikeridediary.domain.maintenance.entity.MaintenanceScheduleEntity;
import com.bikeridediary.domain.maintenance.repository.MaintenanceScheduleRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// 정비 주기 비즈니스 로직
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceScheduleService {

    private final MaintenanceScheduleRepository scheduleRepository;
    private final BikeRepository bikeRepository;

    // 특정 바이크의 모든 정비 주기 조회 (현재 주행거리 기준으로 정비 필요 여부 포함)
    public List<MaintenanceScheduleResponse> getSchedules(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        Integer currentMileage = bikeEntity.getTotalMileageKm();
        return scheduleRepository.findByBikeEntityIdAndDeletedAtIsNull(bikeId)
                .stream()
                .map(schedule -> MaintenanceScheduleResponse.from(schedule, currentMileage))
                .toList();
    }

    // 특정 정비 주기 조회
    public MaintenanceScheduleResponse getSchedule(UUID scheduleId, UUID userId) {
        MaintenanceScheduleEntity entity = findScheduleOrThrow(scheduleId);
        verifyScheduleOwnership(entity, userId);

        Integer currentMileage = entity.getBikeEntity().getTotalMileageKm();
        return MaintenanceScheduleResponse.from(entity, currentMileage);
    }

    // 정비 주기 생성 (동일 바이크에 동일 정비 종류 중복 불가)
    @Transactional
    public MaintenanceScheduleResponse createSchedule(MaintenanceScheduleCreateRequest request, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(request.bikeId());
        verifyBikeOwnership(bikeEntity, userId);

        // 동일 정비 종류 중복 확인
        if (scheduleRepository.existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull(
                request.bikeId(), request.maintenanceType())) {
            throw new BusinessException(ErrorCode.MAINTENANCE_SCHEDULE_DUPLICATE);
        }

        MaintenanceScheduleEntity entity = MaintenanceScheduleEntity.create(
                bikeEntity,
                request.maintenanceType(),
                request.intervalKm(),
                request.intervalMonths(),
                request.lastMaintenanceMileage(),
                request.lastMaintenanceDate()
        );

        MaintenanceScheduleEntity saved = scheduleRepository.save(entity);
        return MaintenanceScheduleResponse.from(saved, bikeEntity.getTotalMileageKm());
    }

    // 정비 주기 수정
    @Transactional
    public MaintenanceScheduleResponse updateSchedule(UUID scheduleId, MaintenanceScheduleUpdateRequest request, UUID userId) {
        MaintenanceScheduleEntity entity = findScheduleOrThrow(scheduleId);
        verifyScheduleOwnership(entity, userId);

        entity.update(
                request.intervalKm(),
                request.intervalMonths(),
                request.lastMaintenanceMileage(),
                request.lastMaintenanceDate()
        );

        Integer currentMileage = entity.getBikeEntity().getTotalMileageKm();
        return MaintenanceScheduleResponse.from(entity, currentMileage);
    }

    // 정비 주기 삭제 (소프트 삭제)
    @Transactional
    public void deleteSchedule(UUID scheduleId, UUID userId) {
        MaintenanceScheduleEntity entity = findScheduleOrThrow(scheduleId);
        verifyScheduleOwnership(entity, userId);

        entity.delete();
    }

    // ============ 헬퍼 메서드 ============

    private BikeEntity findBikeOrThrow(UUID bikeId) {
        return bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));
    }

    private MaintenanceScheduleEntity findScheduleOrThrow(UUID scheduleId) {
        return scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MAINTENANCE_SCHEDULE_NOT_FOUND));
    }

    private void verifyBikeOwnership(BikeEntity bikeEntity, UUID userId) {
        if (!bikeEntity.isOwner(userId)) {
            throw new BusinessException(ErrorCode.BIKE_ACCESS_DENIED);
        }
    }

    private void verifyScheduleOwnership(MaintenanceScheduleEntity entity, UUID userId) {
        if (!entity.isOwner(userId)) {
            throw new BusinessException(ErrorCode.MAINTENANCE_SCHEDULE_ACCESS_DENIED);
        }
    }
}
