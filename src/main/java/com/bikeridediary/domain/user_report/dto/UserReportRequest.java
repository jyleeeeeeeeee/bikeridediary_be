package com.bikeridediary.domain.user_report.dto;

import jakarta.validation.constraints.NotBlank;

public record UserReportRequest(
        @NotBlank(message = "credential은 필수입니다")
        String title,
        @NotBlank(message = "credential은 필수입니다")
        String reportType,
        @NotBlank(message = "credential은 필수입니다")
        String content
) {}
