package com.bikeridediary.domain.bike.service;

import com.bikeridediary.domain.bike.dto.BikeCreateRequest;
import com.bikeridediary.domain.bike.dto.BikeResponse;
import com.bikeridediary.domain.bike.dto.BikeUpdateRequest;
import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BikeService {

    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;

    // 사용자의 모든 활성 바이크 조회 (대표 순서 내림차순, 생성 날짜 내림차순)
    public List<BikeResponse> getMyBikes(UUID userId) {
        verifyUserExists(userId);
        return bikeRepository.findByUserIdAndDeletedAtIsNullOrderByIsRepresentativeDescCreatedAtDesc(userId)
                .stream()
                .map(BikeResponse::from)
                .toList();
    }

    // 특정 바이크 조회 (사용자 소유 확인 필수)
    public BikeResponse getBike(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));

        verifyBikeOwnership(bikeEntity, userId);
        return BikeResponse.from(bikeEntity);
    }

    // 새 바이크 생성 (첫 번째 바이크면 자동으로 대표 설정)
    @Transactional
    public BikeResponse createBike(BikeCreateRequest request, UUID userId) {
        UserEntity userEntity = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BikeEntity bikeEntity = BikeEntity.create(
                userEntity,
                request.manufacturerName(),
                request.modelName(),
                request.year(),
                request.category(),
                request.totalMileageKm()
        );

        // 첫 번째 바이크면 대표로 설정
        long bikeCount = bikeRepository.findByUserIdAndDeletedAtIsNullOrderByIsRepresentativeDescCreatedAtDesc(userId)
                .size();
        if (bikeCount == 0) {
            bikeEntity.setRepresentative(true);
        }

        BikeEntity saved = bikeRepository.save(bikeEntity);
        return BikeResponse.from(saved);
    }

    // 바이크 정보 수정
    @Transactional
    public BikeResponse updateBike(UUID bikeId, BikeUpdateRequest request, UUID userId) {
        BikeEntity bikeEntity = bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));

        verifyBikeOwnership(bikeEntity, userId);

        bikeEntity.update(
                request.manufacturerName(),
                request.modelName(),
                request.year(),
                request.category(),
                request.totalMileageKm(),
                request.purchasedAt(),
                request.memo()
        );

        return BikeResponse.from(bikeRepository.save(bikeEntity));
    }

    // 바이크 삭제 (소프트 삭제)
    @Transactional
    public void deleteBike(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));

        verifyBikeOwnership(bikeEntity, userId);

        // 대표 바이크면 플래그 해제
        if (bikeEntity.isRepresentative()) {
            bikeEntity.setRepresentative(false);
        }

        bikeEntity.delete();
        bikeRepository.save(bikeEntity);
    }

    // 대표 바이크 설정 (기존 대표 바이크의 플래그를 해제 후 새로 설정)
    @Transactional
    public BikeResponse setRepresentative(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));

        verifyBikeOwnership(bikeEntity, userId);

        bikeRepository.clearRepresentative(userId);
        bikeEntity.setRepresentative(true);

        return BikeResponse.from(bikeRepository.save(bikeEntity));
    }

    // ============ 헬퍼 메서드 ============

    private void verifyUserExists(UUID userId) {
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void verifyBikeOwnership(BikeEntity bikeEntity, UUID userId) {
        if (!bikeEntity.isOwner(userId)) {
            throw new BusinessException(ErrorCode.BIKE_ACCESS_DENIED);
        }
    }
}
