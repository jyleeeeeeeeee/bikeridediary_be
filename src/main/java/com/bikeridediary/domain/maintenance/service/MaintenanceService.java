package com.bikeridediary.domain.maintenance.service;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.fueling.repository.FuelingRepository;
import com.bikeridediary.domain.maintenance.dto.MaintenanceCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceUpdateRequest;
import com.bikeridediary.domain.maintenance.entity.MaintenanceEntity;
import com.bikeridediary.domain.maintenance.repository.MaintenanceRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import com.bikeridediary.utils.ImageStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 정비 기록 비즈니스 로직
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceService {

    private final ImageStorageService imageStorageService;
    private final MaintenanceRepository maintenanceRepository;
    private final BikeRepository bikeRepository;
    private final FuelingRepository fuelingRepository;
    private final ObjectMapper objectMapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    @Value("${file.base-url:http://localhost:8081/files}")
    private String baseUrl;

    // 특정 바이크의 모든 정비 기록 조회
    public List<MaintenanceResponse> getMaintenances(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = findBikeOrThrow(bikeId);
        verifyBikeOwnership(bikeEntity, userId);

        return maintenanceRepository.findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc(bikeId)
                .stream()
                .map(this::responseMaintenance)
                .toList();
    }

    // 특정 정비 기록 조회
    public MaintenanceResponse getMaintenance(UUID maintenanceId, UUID userId) {
        MaintenanceEntity entity = findMaintenanceOrThrow(maintenanceId);
        verifyMaintenanceOwnership(entity, userId);
        return responseMaintenance(entity);
    }

    // 정비 기록 생성
    @Transactional
    public MaintenanceResponse createMaintenance(MaintenanceCreateRequest request, List<MultipartFile> images, UUID userId) {
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
                request.nextDueDate(),
                getStringImageUrls(images, userId)
        );

        MaintenanceEntity saved = maintenanceRepository.save(entity);
        updateBikeMileage(bikeEntity);
        return responseMaintenance(saved);
    }

    // 정비 기록 수정
    @Transactional
    public MaintenanceResponse updateMaintenance(UUID maintenanceId, MaintenanceUpdateRequest request, List<MultipartFile> images, UUID userId) {
        MaintenanceEntity entity = findMaintenanceOrThrow(maintenanceId);
        verifyMaintenanceOwnership(entity, userId);

        // 1. 삭제된 이미지 파일 정리
        List<String> oldUrls = parseStringToList(entity.getImageUrls());
        List<String> keepUrls = request.existingImageUrls() != null ? request.existingImageUrls() : List.of();

        oldUrls.stream()
                .filter(url -> !keepUrls.contains(url))
                .forEach(imageStorageService::delete);

        entity.update(
                request.maintenanceType(),
                request.maintenanceDate(),
                request.mileageAtMaintenance(),
                request.cost(),
                request.description(),
                request.nextDueKm(),
                request.nextDueDate(),
                addImageUrls(keepUrls, images, userId)
        );

        updateBikeMileage(entity.getBikeEntity());
        return responseMaintenance(entity);
    }

    // 정비 기록 삭제 (소프트 삭제)
    @Transactional
    public void deleteMaintenance(UUID maintenanceId, UUID userId) {
        MaintenanceEntity entity = findMaintenanceOrThrow(maintenanceId);
        verifyMaintenanceOwnership(entity, userId);
        BikeEntity bikeEntity = entity.getBikeEntity();
        entity.delete();
        updateBikeMileage(bikeEntity);
    }


    // ============ 이미지 헬퍼 ============


    // String To List
    private List<String> parseStringToList(String str) {
        List<String> list;
        if (str == null || str.isBlank() || "[]".equals(str)) {
            list = List.of();
        } else {
            try {
                list = objectMapper.readValue(str, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                list = List.of();
            }
        }
        return list;
    }

    private String addImageUrls(List<String> urlList, List<MultipartFile> images, UUID userId) {
        List<String> result = new ArrayList<>(urlList);
        if (images != null && !images.isEmpty()) {
            for (MultipartFile img : images) {
                try {
                    result.add(imageStorageService.upload(img, userId.toString()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return toJson(result);
    }

    // ============ 헬퍼 메서드 ============

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

    // 응답 객체 생성
    private MaintenanceResponse responseMaintenance(MaintenanceEntity entity) {
        // 응답 객체에 전달할 image url List에 담아서 생성
        String imageUrls = entity.getImageUrls();
        List<String> images = parseStringToList(imageUrls);
        return MaintenanceResponse.from(entity, images);
    }


    private String toJson(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    // List에 담긴 이미지 파일 URL 생성, DB 저장
    private String getStringImageUrls(List<MultipartFile> images, UUID userId) {
        String imageUrls = "[]";
        if (images != null && !images.isEmpty()) {
            List<String> urlList = new ArrayList<>();
            images.forEach(img -> {
                try {
                    urlList.add(imageStorageService.upload(img, userId.toString()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            imageUrls = toJson(urlList);
        }
        return imageUrls;
    }
}
