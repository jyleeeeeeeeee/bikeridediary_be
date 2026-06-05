package com.bikeridediary.domain.maintenance.controller;

import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleUpdateRequest;
import com.bikeridediary.domain.maintenance.service.MaintenanceScheduleService;
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

@Tag(name = "정비 주기", description = "정비 주기 설정 및 정비 필요 여부 확인 API")
@RestController
@RequestMapping("/api/v1/maintenance-schedules")
@RequiredArgsConstructor
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService scheduleService;

    @Operation(summary = "정비 주기 목록 조회", description = "특정 바이크의 모든 정비 주기를 조회합니다. 현재 주행거리 기준 정비 필요 여부(overdue)를 포함합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MaintenanceScheduleResponse>>> getSchedules(
            @RequestParam UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        List<MaintenanceScheduleResponse> schedules = scheduleService.getSchedules(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(schedules));
    }

    @Operation(summary = "정비 주기 상세 조회", description = "특정 정비 주기의 상세 정보를 조회합니다.")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> getSchedule(
            @PathVariable UUID scheduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        MaintenanceScheduleResponse schedule = scheduleService.getSchedule(scheduleId, userId);
        return ResponseEntity.ok(ApiResponse.ok(schedule));
    }

    @Operation(summary = "정비 주기 생성", description = "새로운 정비 주기를 생성합니다. 동일 바이크에 동일 정비 종류의 주기는 중복 등록할 수 없습니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> createSchedule(
            @Valid @RequestBody MaintenanceScheduleCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        MaintenanceScheduleResponse schedule = scheduleService.createSchedule(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(schedule));
    }

    @Operation(summary = "정비 주기 수정", description = "정비 주기를 수정합니다. 정비 종류는 변경할 수 없습니다.")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> updateSchedule(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody MaintenanceScheduleUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        MaintenanceScheduleResponse schedule = scheduleService.updateSchedule(scheduleId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(schedule));
    }

    @Operation(summary = "정비 주기 삭제", description = "정비 주기를 삭제합니다. (소프트 삭제)")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable UUID scheduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
