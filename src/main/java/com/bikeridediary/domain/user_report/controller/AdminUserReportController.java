package com.bikeridediary.domain.user_report.controller;

import com.bikeridediary.domain.user_report.dto.UserReportRequest;
import com.bikeridediary.domain.user_report.dto.UserReportUpdateRequest;
import com.bikeridediary.domain.user_report.service.UserReportService;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "유저 제보", description = "버그/장소 삭제/기타 제보")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/user-reports")
public class AdminUserReportController {
    private final UserReportService userReportService;

    @Operation(summary = "제보 처리")
    @PatchMapping("/{reportId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateReport(@PathVariable UUID reportId,
                                                          @RequestBody UserReportUpdateRequest request,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        userReportService.updateReport(reportId, userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
