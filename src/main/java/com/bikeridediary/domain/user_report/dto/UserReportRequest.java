package com.bikeridediary.domain.user_report.dto;

import com.bikeridediary.domain.user_report.entity.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;


public record UserReportRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Length(max = 100, message = "제목은 100자 이내로 작성해주세요")
        String title,

        @NotNull(message = "요청 종류는 필수입니다")
        ReportType reportType,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        // reportType=PLACE_DELETE일 때만 필요, 그 외 null 허용
        UUID targetPlaceId
) {}