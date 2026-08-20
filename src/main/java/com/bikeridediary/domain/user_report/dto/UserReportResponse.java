package com.bikeridediary.domain.user_report.dto;

import com.bikeridediary.domain.user_report.entity.ReportStatus;
import com.bikeridediary.domain.user_report.entity.ReportType;
import com.bikeridediary.domain.user_report.entity.UserReportEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserReportResponse(
        UUID id,
        String title,
        ReportType reportType,
        String content,
        ReportStatus status,
        String reply,
        UUID targetPlaceId,
        LocalDateTime createdAt,
        LocalDateTime endedAt
) {
    public static UserReportResponse from(UserReportEntity entity) {
        return new UserReportResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getReportType(),
                entity.getContent(),
                entity.getStatus(),
                entity.getReply(),
                entity.getTargetPlace() == null ? null : entity.getTargetPlace().getId(),
                entity.getCreatedAt(),
                entity.getEndedAt()
        );
    }
}