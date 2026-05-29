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

    /**
     * Get all active bikes for the user, ordered by representative desc, then created date desc.
     */
    public List<BikeResponse> getMyBikes(UUID userId) {
        verifyUserExists(userId);
        return bikeRepository.findByUserIdAndDeletedAtIsNullOrderByIsRepresentativeDescCreatedAtDesc(userId)
                .stream()
                .map(BikeResponse::from)
                .toList();
    }

    /**
     * Get a specific bike by ID (must be owned by the user).
     */
    public BikeResponse getBike(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));

        verifyBikeOwnership(bikeEntity, userId);
        return BikeResponse.from(bikeEntity);
    }

    /**
     * Create a new bike for the user.
     * If it's the first bike, automatically set it as representative.
     */
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

    /**
     * Update an existing bike.
     */
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

    /**
     * Soft delete a bike (set deleted_at).
     */
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

    /**
     * Set a bike as the representative bike.
     * Clears the representative flag from all other bikes of the user.
     */
    @Transactional
    public BikeResponse setRepresentative(UUID bikeId, UUID userId) {
        BikeEntity bikeEntity = bikeRepository.findByIdAndDeletedAtIsNull(bikeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));

        verifyBikeOwnership(bikeEntity, userId);

        bikeRepository.clearRepresentative(userId);
        bikeEntity.setRepresentative(true);

        return BikeResponse.from(bikeRepository.save(bikeEntity));
    }

    // ============ Helper methods ============

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
