package com.bikeridediary.domain.place_category.repository;

import com.bikeridediary.domain.place_category.entity.PlaceCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategoryEntity, String> {
}
