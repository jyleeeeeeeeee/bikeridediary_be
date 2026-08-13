package com.bikeridediary.infra.opinet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<AreaEntity, AreaEntity.AreaId> {
}
