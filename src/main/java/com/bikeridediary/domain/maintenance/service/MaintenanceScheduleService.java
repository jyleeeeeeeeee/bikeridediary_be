package com.bikeridediary.domain.maintenance.service;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleSyncRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleUpdateRequest;
import com.bikeridediary.domain.maintenance.entity.MaintenanceEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceScheduleEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import com.bikeridediary.domain.maintenance.repository.MaintenanceRepository;
import com.bikeridediary.domain.maintenance.repository.MaintenanceScheduleRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 정비 주기 비즈니스 로직
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceScheduleService {

    private final MaintenanceScheduleRepository scheduleRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final BikeRepository bikeRepository;
    private final com.bikeridediary.domain.user.repository.UserRepository userRepository;

    // 특정 바이크의 모든 정비 주기 조회 (정비 기록 기반으로 overdue 판단)
    public List<MaintenanceScheduleResponse> getSchedules(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        Long currentMileage = bikeEntity.getTotalMileageKm();
        return scheduleRepository.findByBikeEntityIdAndDeletedAtIsNull(bikeId)
                .stream()
                .map(schedule -> buildResponse(schedule, bikeId, currentMileage))
                .toList();
    }

    // 특정 정비 주기 조회
    public MaintenanceScheduleResponse getSchedule(UUID scheduleId, UUID userId) {
        MaintenanceScheduleEntity entity = findScheduleOrThrow(scheduleId);
        verifyScheduleOwnership(entity, userId);

        UUID bikeId = entity.getBikeEntity().getId();
        Long currentMileage = entity.getBikeEntity().getTotalMileageKm();
        return buildResponse(entity, bikeId, currentMileage);
    }

    // 정비 주기 생성 (동일 바이크에 동일 정비 종류 중복 불가)
    @Transactional
    public MaintenanceScheduleResponse createSchedule(MaintenanceScheduleCreateRequest request, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(request.bikeId());
        verifyBikeOwnership(bikeEntity, userId);

        if (scheduleRepository.existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull(
                request.bikeId(), request.maintenanceType())) {
            throw new BusinessException(ErrorCode.MAINTENANCE_SCHEDULE_DUPLICATE);
        }

        MaintenanceScheduleEntity entity = MaintenanceScheduleEntity.create(
                bikeEntity,
                request.maintenanceType(),
                request.intervalKm(),
                request.intervalMonths()
        );

        MaintenanceScheduleEntity saved = scheduleRepository.save(entity);
        return buildResponse(saved, request.bikeId(), bikeEntity.getTotalMileageKm());
    }

    // 정비 주기 수정
    @Transactional
    public MaintenanceScheduleResponse updateSchedule(UUID scheduleId, MaintenanceScheduleUpdateRequest request, UUID userId) {
        MaintenanceScheduleEntity entity = findScheduleOrThrow(scheduleId);
        verifyScheduleOwnership(entity, userId);

        entity.update(request.intervalKm(), request.intervalMonths());

        UUID bikeId = entity.getBikeEntity().getId();
        Long currentMileage = entity.getBikeEntity().getTotalMileageKm();
        return buildResponse(entity, bikeId, currentMileage);
    }

    // 정비 주기 삭제 (소프트 삭제)
    @Transactional
    public void deleteSchedule(UUID scheduleId, UUID userId) {
        MaintenanceScheduleEntity entity = findScheduleOrThrow(scheduleId);
        verifyScheduleOwnership(entity, userId);

        entity.delete();
    }

    // ============ 헬퍼 메서드 ============

    private MaintenanceScheduleResponse buildResponse(
            MaintenanceScheduleEntity schedule, UUID bikeId, Long currentMileage) {
        MaintenanceType type = schedule.getMaintenanceType();

        // km 기준: 주행거리가 가장 높은 기록
        var highestMileageRecord = maintenanceRepository
                .findTopByBikeIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMileageAtMaintenanceDesc(bikeId, type);
        // 날짜 기준: 가장 최근 날짜의 기록
        var latestDateRecord = maintenanceRepository
                .findTopByBikeIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMaintenanceDateDesc(bikeId, type);

        Long lastMileage = highestMileageRecord.map(MaintenanceEntity::getMileageAtMaintenance).orElse(null);
        LocalDate lastDate = latestDateRecord.map(MaintenanceEntity::getMaintenanceDate).orElse(null);

        return MaintenanceScheduleResponse.from(schedule, currentMileage, lastMileage, lastDate);
    }

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

    @Transactional
    public MaintenanceScheduleResponse sync(UUID userId, MaintenanceScheduleSyncRequest req) {
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BikeEntity bike = bikeRepository.findById(req.bikeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));
        if (!bike.isOwner(userId)) {
            throw new BusinessException(ErrorCode.MAINTENANCE_SCHEDULE_ACCESS_DENIED);
        }

        Optional<MaintenanceScheduleEntity> existingOpt =
                scheduleRepository.findById(req.id());
        MaintenanceScheduleEntity target;

        if (existingOpt.isPresent()) {
            MaintenanceScheduleEntity existing = existingOpt.get();
            if (!existing.isOwner(userId)) {
                throw new BusinessException(ErrorCode.MAINTENANCE_SCHEDULE_ACCESS_DENIED);
            }
            if (req.deletedAt() != null) {
                if (!existing.isDeleted()) existing.delete();
                return buildResponse(existing, bike.getId(), bike.getTotalMileageKm());
            }
            if (!req.updatedAt().isAfter(existing.getUpdatedAt())) {
                // 서버가 더 최신 — 반영 안 함
                return buildResponse(existing, bike.getId(), bike.getTotalMileageKm());
            }
            existing.update(req.intervalKm(), req.intervalMonths());
            target = existing;
        } else {
            target = MaintenanceScheduleEntity.createWithId(
                    req.id(), bike, req.maintenanceType(),
                    req.intervalKm(), req.intervalMonths()
            );
            scheduleRepository.save(target);
        }

        return buildResponse(target, bike.getId(), bike.getTotalMileageKm());
    }

    // 초기 pull용 — 유저의 모든 활성 스케줄
    public List<MaintenanceScheduleResponse> getMySchedules(UUID userId) {
        return scheduleRepository.findMySchedules(userId).stream()
                .map(s -> buildResponse(
                        s,
                        s.getBikeEntity().getId(),
                        s.getBikeEntity().getTotalMileageKm()))
                .toList();
    }
}
