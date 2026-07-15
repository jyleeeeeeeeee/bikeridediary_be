package com.bikeridediary.domain.place.repository;

import com.bikeridediary.domain.place.entity.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {
    List<PlaceEntity> findByDeletedAtIsNullOrderByPlaceCategoryEntity_DisplayOrderAsc();

    List<PlaceEntity> findByPlaceCategoryEntity_CategoryCodeAndDeletedAtIsNull(String categoryCode);
}
