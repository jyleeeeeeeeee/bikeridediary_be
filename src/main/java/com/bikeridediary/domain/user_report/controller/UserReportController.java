package com.bikeridediary.domain.user_report.controller;

import com.bikeridediary.domain.user_report.dto.UserReportRequest;
import com.bikeridediary.domain.user_report.dto.UserReportResponse;
import com.bikeridediary.domain.user_report.service.UserReportService;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "버그/장소 삭제 제보")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-reports")
public class UserReportController {
    private final UserReportService userReportService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> report(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody @Valid UserReportRequest request) {
        userReportService.report(request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
