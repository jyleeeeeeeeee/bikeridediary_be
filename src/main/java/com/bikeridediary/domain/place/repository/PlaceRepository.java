package com.bikeridediary.domain.place.repository;

import com.bikeridediary.domain.place.entity.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {
    List<PlaceEntity> findByDeletedAtIsNullOrderByPlaceCategoryEntity_DisplayOrderAsc();

    List<PlaceEntity> findByPlaceCategoryEntity_CategoryCodeAndDeletedAtIsNull(String categoryCode);

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


}
