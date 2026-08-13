package com.bikeridediary.domain.place.repository;

import com.bikeridediary.domain.place.entity.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {
    List<PlaceEntity> findByDeletedAtIsNullOrderByPlaceCategoryEntity_DisplayOrderAsc();

    List<PlaceEntity> findByPlaceCategoryEntity_CategoryCodeAndDeletedAtIsNull(String categoryCode);

    // no(자동 증가 조회 번호)로 특정 place 조회 — DB 관리/디버깅용
    Optional<PlaceEntity> findByNo(Long no);

    @Query("""
            SELECT p FROM PlaceEntity p
            WHERE p.deletedAt IS NULL
              AND p.placeName = :placeName
              AND p.latitude  BETWEEN :minLat AND :maxLat
              AND p.longitude BETWEEN :minLng AND :maxLng
            """)
    List<PlaceEntity> findNearbyByName(
            @Param("placeName") String placeName,
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng
    );

    // Postgres 표준 GROUP BY 규칙: SELECT 목록의 non-aggregate 컬럼은 모두 GROUP BY에 있어야 함.
    // nickname 별칭 지정으로 Projection 인터페이스의 getNickname()과 매핑.
    @Query("""
              SELECT p.userEntity.id       AS userId,
                     p.userEntity.nickname AS nickname,
                     COUNT(p)              AS count
              FROM PlaceEntity p
              WHERE p.deletedAt IS NULL
                AND p.userEntity IS NOT NULL
              GROUP BY p.userEntity.id, p.userEntity.nickname
              ORDER BY COUNT(p) DESC
              """)
    List<PlaceRegistrationCount> countRegistrationsByUser();

    Optional<PlaceEntity> findByIdAndDeletedAtIsNull(UUID id);

}
