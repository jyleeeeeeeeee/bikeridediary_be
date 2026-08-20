package com.bikeridediary.domain.user_report.repository;

import com.bikeridediary.domain.user_report.entity.ReportStatus;
import com.bikeridediary.domain.user_report.entity.UserReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserReportRepository extends JpaRepository<UserReportEntity, UUID> {

    // 유저별 제보 내역
    List<UserReportEntity> findByUserEntity_Id(UUID userId);

    // 제보 내역 상세 조회
    Optional<UserReportEntity> findByIdAndUserEntity_Id(UUID reportId, UUID userId);


    // 유저별 제보 내역 (내 목록)
    Page<UserReportEntity> findByUserEntity_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // 관리자 큐 (상태별 최신순)
    Page<UserReportEntity> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    Optional<UserReportEntity> findByIdAndDeletedAtIsNull(UUID reportId);

}
