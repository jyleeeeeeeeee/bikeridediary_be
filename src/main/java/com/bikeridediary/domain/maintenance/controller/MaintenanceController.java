package com.bikeridediary.domain.maintenance.controller;

import com.bikeridediary.domain.maintenance.dto.MaintenanceCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceUpdateRequest;
import com.bikeridediary.domain.maintenance.service.MaintenanceService;
import com.bikeridediary.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.bikeridediary.global.auth.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// 정비 기록 관리 API 컨트롤러
@RestController
@RequestMapping("/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // 특정 바이크의 모든 정비 기록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<MaintenanceResponse>>> getMaintenances(
            @RequestParam UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        List<MaintenanceResponse> maintenances = maintenanceService.getMaintenances(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(maintenances));
    }

    // 특정 정비 기록 조회
    @GetMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> getMaintenance(
            @PathVariable UUID maintenanceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        MaintenanceResponse maintenance = maintenanceService.getMaintenance(maintenanceId, userId);
        return ResponseEntity.ok(ApiResponse.ok(maintenance));
    }

    // 정비 기록 생성
    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceResponse>> createMaintenance(
            @Valid @RequestBody MaintenanceCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        MaintenanceResponse maintenance = maintenanceService.createMaintenance(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(maintenance));
    }

    // 정비 기록 수정
    @PutMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> updateMaintenance(
            @PathVariable UUID maintenanceId,
            @Valid @RequestBody MaintenanceUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        MaintenanceResponse maintenance = maintenanceService.updateMaintenance(maintenanceId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(maintenance));
    }

    // 정비 기록 삭제 (소프트 삭제)
    @DeleteMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenance(
            @PathVariable UUID maintenanceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        maintenanceService.deleteMaintenance(maintenanceId, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
