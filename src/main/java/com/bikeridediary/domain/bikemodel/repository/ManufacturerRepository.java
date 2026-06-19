package com.bikeridediary.domain.bikemodel.repository;

import com.bikeridediary.domain.bikemodel.entity.ManufacturerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManufacturerRepository extends JpaRepository<ManufacturerEntity, String> {

    List<ManufacturerEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
}
