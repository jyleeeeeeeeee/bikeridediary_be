package com.bikeridediary.domain.user_report.repository;

import com.bikeridediary.domain.user_report.entity.UserReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserReportRepository extends JpaRepository<UserReportEntity, UUID> {

    // 유저별 제보 내역
    List<UserReportEntity> findByUserEntity_Id(UUID userId);

    // 제보 내역 상세 조회
    Optional<UserReportEntity> findByIdAndUserEntity_Id(UUID reportId, UUID userId);
}
