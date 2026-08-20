package com.bikeridediary.domain.user_report.dto;

import com.bikeridediary.domain.user_report.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record UserReportUpdateRequest(

        @NotNull(message = "상태값은 필수입니다.")

        ReportStatus status,

        String reply
) {}