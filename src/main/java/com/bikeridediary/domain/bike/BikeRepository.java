package com.bikeridediary.domain.bike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BikeRepository extends JpaRepository<BikeEntity, UUID> {

    // 사용자의 모든 활성 바이크 조회
    List<BikeEntity> findByUserIdAndDeletedAtIsNullOrderByIsRepresentativeDescCreatedAtDesc(UUID userId);

    // 특정 활성 바이크 조회
    Optional<BikeEntity> findByIdAndDeletedAtIsNull(UUID id);

    // 대표 바이크 조회
    Optional<BikeEntity> findByUserIdAndIsRepresentativeTrueAndDeletedAtIsNull(UUID userId);

    // 사용자의 모든 바이크에서 대표 플래그 해제 (새로운 대표 설정 전에 호출)
    @Modifying
    @Query("UPDATE BikeEntity b SET b.isRepresentative = false WHERE b.user.id = :userId")
    void clearRepresentative(@Param("userId") UUID userId);
}
