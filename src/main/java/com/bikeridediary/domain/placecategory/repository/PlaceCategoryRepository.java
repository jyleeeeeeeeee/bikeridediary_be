package com.bikeridediary.domain.placecategory.repository;

import com.bikeridediary.domain.placecategory.entity.PlaceCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategoryEntity, String> {

    // no(자동 증가 조회 번호)로 특정 category 조회 — DB 관리/디버깅용
    Optional<PlaceCategoryEntity> findByNo(Long no);
}
