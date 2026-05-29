package com.bikeridediary.domain.maintenance.controller;

import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleUpdateRequest;
import com.bikeridediary.domain.maintenance.service.MaintenanceScheduleService;
import com.bikeridediary.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// 정비 주기 관리 API 컨트롤러
@RestController
@RequestMapping("/api/v1/maintenance-schedules")
@RequiredArgsConstructor
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService scheduleService;

    // 특정 바이크의 모든 정비 주기 조회 (정비 필요 여부 포함)
    @GetMapping
    public ResponseEntity<ApiResponse<List<MaintenanceScheduleResponse>>> getSchedules(
            @RequestParam UUID bikeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<MaintenanceScheduleResponse> schedules = scheduleService.getSchedules(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(schedules));
    }

    // 특정 정비 주기 조회
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> getSchedule(
            @PathVariable UUID scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        MaintenanceScheduleResponse schedule = scheduleService.getSchedule(scheduleId, userId);
        return ResponseEntity.ok(ApiResponse.ok(schedule));
    }

    // 정비 주기 생성 (동일 바이크에 동일 정비 종류 중복 불가)
    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> createSchedule(
            @Valid @RequestBody MaintenanceScheduleCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        MaintenanceScheduleResponse schedule = scheduleService.createSchedule(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(schedule));
    }

    // 정비 주기 수정
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> updateSchedule(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody MaintenanceScheduleUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        MaintenanceScheduleResponse schedule = scheduleService.updateSchedule(scheduleId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(schedule));
    }

    // 정비 주기 삭제 (소프트 삭제)
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable UUID scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
