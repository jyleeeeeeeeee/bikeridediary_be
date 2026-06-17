package com.bikeridediary.domain.bikemodel.repository;

import com.bikeridediary.domain.bikemodel.entity.BikeModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BikeModelRepository extends JpaRepository<BikeModelEntity, Long> {

    List<BikeModelEntity> findByManufacturerIdOrderByNameAsc(Long manufacturerId);

    boolean existsByManufacturerIdAndNameAndYear(Long manufacturerId, String name, Integer year);

    @Query("SELECT DISTINCT b.name FROM BikeModelEntity b WHERE b.manufacturer.id = :manufacturerId ORDER BY b.name ASC")
    List<String> findDistinctModelNamesByManufacturerId(@Param("manufacturerId") Long manufacturerId);

    @Query("SELECT b FROM BikeModelEntity b WHERE b.manufacturer.id = :manufacturerId AND b.name = :modelName ORDER BY b.year DESC")
    List<BikeModelEntity> findByManufacturerIdAndName(@Param("manufacturerId") Long manufacturerId, @Param("modelName") String modelName);
}
