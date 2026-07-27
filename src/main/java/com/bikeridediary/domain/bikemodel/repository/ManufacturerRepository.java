package com.bikeridediary.domain.bikemodel.repository;

import com.bikeridediary.domain.bikemodel.entity.ManufacturerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManufacturerRepository extends JpaRepository<ManufacturerEntity, String> {

    List<ManufacturerEntity> findByIsActiveTrueOrderByDisplayOrderAsc();

    // no(자동 증가 조회 번호)로 특정 manufacturer 조회 — DB 관리/디버깅용
    Optional<ManufacturerEntity> findByNo(Long no);
}
