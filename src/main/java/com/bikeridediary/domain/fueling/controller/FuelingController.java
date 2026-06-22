package com.bikeridediary.domain.fueling.controller;

import com.bikeridediary.domain.fueling.dto.*;
import com.bikeridediary.domain.fueling.service.FuelingService;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "주유 기록", description = "주유 이력 관리 및 연비 계산 API")
@RestController
@RequestMapping("/api/v1/fuelings")
@RequiredArgsConstructor
public class FuelingController {

    private final FuelingService fuelingService;

    @Operation(summary = "주유 기록 목록 조회", description = "특정 바이크의 모든 주유 기록을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FuelingResponse>>> getFuelings(
            @RequestParam UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        List<FuelingResponse> fuelings = fuelingService.getFuelings(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(fuelings));
    }

    @Operation(summary = "주유 기록 상세 조회", description = "특정 주유 기록의 상세 정보를 조회합니다.")
    @GetMapping("/{fuelingId}")
    public ResponseEntity<ApiResponse<FuelingResponse>> getFueling(
            @PathVariable UUID fuelingId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        FuelingResponse fueling = fuelingService.getFueling(fuelingId, userId);
        return ResponseEntity.ok(ApiResponse.ok(fueling));
    }

    @Operation(summary = "주유 기록 생성", description = "새로운 주유 기록을 생성합니다. 이전 주유 기록이 있으면 연비가 자동 계산됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<FuelingResponse>> createFueling(
            @Valid @RequestBody FuelingCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        FuelingResponse fueling = fuelingService.createFueling(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(fueling));
    }

    @Operation(summary = "주유 기록 수정", description = "주유 기록을 수정합니다. 수정 시 연비가 재계산됩니다.")
    @PutMapping("/{fuelingId}")
    public ResponseEntity<ApiResponse<FuelingResponse>> updateFueling(
            @PathVariable UUID fuelingId,
            @Valid @RequestBody FuelingUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        FuelingResponse fueling = fuelingService.updateFueling(fuelingId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(fueling));
    }

    @Operation(summary = "주유 기록 삭제", description = "주유 기록을 삭제합니다. (소프트 삭제)")
    @DeleteMapping("/{fuelingId}")
    public ResponseEntity<ApiResponse<Void>> deleteFueling(
            @PathVariable UUID fuelingId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        fuelingService.deleteFueling(fuelingId, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "주유 통계 조회", description = "특정 바이크의 주유 통계(평균 연비, 총 비용, 평균 유가 등)를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FuelingStatsResponse>> getStats(
            @RequestParam UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        FuelingStatsResponse stats = fuelingService.getStats(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
