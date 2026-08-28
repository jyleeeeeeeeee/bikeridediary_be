package com.bikeridediary.domain.bikemodel.repository;

import com.bikeridediary.domain.bikemodel.dto.BikeModelNameResponse;
import com.bikeridediary.domain.bikemodel.entity.BikeModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BikeModelRepository extends JpaRepository<BikeModelEntity, Long> {

    // no(자동 증가 조회 번호)로 특정 bike_model 조회 — DB 관리/디버깅용
    Optional<BikeModelEntity> findByNo(Long no);

    boolean existsByManufacturerManufacturerNameAndName(String manufacturerName, String modelName);

    @Query("SELECT new com.bikeridediary.domain.bikemodel.dto.BikeModelNameResponse(b.name, b.type) " +
           "FROM BikeModelEntity b WHERE b.manufacturer.manufacturerName = :manufacturerName " +
           "GROUP BY b.name, b.type ORDER BY b.name ASC")
    List<BikeModelNameResponse> findDistinctModelNamesWithTypeByManufacturerName(@Param("manufacturerName") String manufacturerName);

    @Query("SELECT b FROM BikeModelEntity b WHERE b.manufacturer.manufacturerName = :manufacturerName AND b.name = :modelName ORDER BY b.year DESC")
    List<BikeModelEntity> findByManufacturerNameAndModelName(@Param("manufacturerName") String manufacturerName, @Param("modelName") String modelName);

}
