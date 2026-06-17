package com.bikeridediary.domain.bikemodel.repository;

import com.bikeridediary.domain.bikemodel.entity.ManufacturerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManufacturerRepository extends JpaRepository<ManufacturerEntity, Long> {

    Optional<ManufacturerEntity> findByApiName(String apiName);

    List<ManufacturerEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
}
