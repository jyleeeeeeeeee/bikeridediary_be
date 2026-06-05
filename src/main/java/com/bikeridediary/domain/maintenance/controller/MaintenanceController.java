package com.bikeridediary.domain.maintenance.controller;

import com.bikeridediary.domain.maintenance.dto.MaintenanceCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceUpdateRequest;
import com.bikeridediary.domain.maintenance.service.MaintenanceService;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.bikeridediary.global.auth.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "정비 기록", description = "소모품 교체 이력 관리 API")
@RestController
@RequestMapping("/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @Operation(summary = "정비 기록 목록 조회", description = "특정 바이크의 모든 정비 기록을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MaintenanceResponse>>> getMaintenances(
            @RequestParam UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        List<MaintenanceResponse> maintenances = maintenanceService.getMaintenances(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(maintenances));
    }

    @Operation(summary = "정비 기록 상세 조회", description = "특정 정비 기록의 상세 정보를 조회합니다.")
    @GetMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> getMaintenance(
            @PathVariable UUID maintenanceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        MaintenanceResponse maintenance = maintenanceService.getMaintenance(maintenanceId, userId);
        return ResponseEntity.ok(ApiResponse.ok(maintenance));
    }

    @Operation(summary = "정비 기록 생성", description = "새로운 정비 기록을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceResponse>> createMaintenance(
            @Valid @RequestBody MaintenanceCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        MaintenanceResponse maintenance = maintenanceService.createMaintenance(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(maintenance));
    }

    @Operation(summary = "정비 기록 수정", description = "정비 기록을 수정합니다.")
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

    @Operation(summary = "정비 기록 삭제", description = "정비 기록을 삭제합니다. (소프트 삭제)")
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
