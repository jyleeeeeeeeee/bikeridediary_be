package com.bikeridediary.domain.place.repository;

import com.bikeridediary.domain.place.entity.PlaceWishEntity;
import com.bikeridediary.domain.place.entity.PlaceWishId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlaceWishRepository extends JpaRepository<PlaceWishEntity, PlaceWishId> {

    @Query("""
          SELECT w FROM PlaceWishEntity w
          JOIN FETCH w.placeEntity p
          JOIN FETCH p.placeCategoryEntity c
          WHERE w.id.userId = :userId
            AND p.deletedAt IS NULL
          ORDER BY w.createdAt DESC
          """)
    List<PlaceWishEntity> findByIdUserIdWithPlace(@Param("userId") UUID userId);

    @Query("""
        SELECT w.id.placeId FROM PlaceWishEntity w
        WHERE w.id.userId = :userId
        AND w.id.placeId IN :placeIds
    """)
    List<PlaceWishEntity> findWishedPlaceIdsIn(
            @Param("userId") UUID userId,
            @Param("placeIds") List<UUID> placeIds
    );

    // 유저의 찜한 place_id 전량 조회 (Set으로 바로 감싸 isWished 매핑에 사용).
    // 지도(찾아보기)는 페이징 없이 places 전체 로드 + in-memory 필터 UX라
    // IN절로 좁힐 실익 없음 (유저별 wish 수는 많아야 수십~수백).
    // SELECT w.id.placeId 프로젝션으로 wish 엔티티 전체를 로드하지 않음.
    @Query("SELECT w.id.placeId FROM PlaceWishEntity w WHERE w.id.userId = :userId")
    List<UUID> findPlaceIdsByUserId(@Param("userId") UUID userId);
}
