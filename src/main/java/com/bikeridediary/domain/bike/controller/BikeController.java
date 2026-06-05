package com.bikeridediary.domain.bike.controller;

import com.bikeridediary.domain.bike.dto.BikeCreateRequest;
import com.bikeridediary.domain.bike.dto.BikeResponse;
import com.bikeridediary.domain.bike.dto.BikeUpdateRequest;
import com.bikeridediary.domain.bike.service.BikeService;
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

@Tag(name = "바이크", description = "바이크 등록, 조회, 수정, 삭제 API")
@RestController
@RequestMapping("/api/v1/bikes")
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    @Operation(summary = "내 바이크 목록 조회", description = "인증된 사용자의 모든 바이크를 대표 바이크 우선으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BikeResponse>>> getMyBikes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        List<BikeResponse> bikes = bikeService.getMyBikes(userId);
        return ResponseEntity.ok(ApiResponse.ok(bikes));
    }

    @Operation(summary = "바이크 상세 조회", description = "특정 바이크의 상세 정보를 조회합니다.")
    @GetMapping("/{bikeId}")
    public ResponseEntity<ApiResponse<BikeResponse>> getBike(
            @PathVariable UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        BikeResponse bike = bikeService.getBike(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(bike));
    }

    @Operation(summary = "바이크 등록", description = "새로운 바이크를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<BikeResponse>> createBike(
            @Valid @RequestBody BikeCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        BikeResponse bike = bikeService.createBike(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(bike));
    }

    @Operation(summary = "바이크 수정", description = "바이크 정보를 수정합니다.")
    @PutMapping("/{bikeId}")
    public ResponseEntity<ApiResponse<BikeResponse>> updateBike(
            @PathVariable UUID bikeId,
            @Valid @RequestBody BikeUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        BikeResponse bike = bikeService.updateBike(bikeId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(bike));
    }

    @Operation(summary = "바이크 삭제", description = "바이크를 삭제합니다. (소프트 삭제)")
    @DeleteMapping("/{bikeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBike(
            @PathVariable UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        bikeService.deleteBike(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "대표 바이크 설정", description = "특정 바이크를 대표 바이크로 설정합니다. 기존 대표 바이크는 해제됩니다.")
    @PatchMapping("/{bikeId}/representative")
    public ResponseEntity<ApiResponse<BikeResponse>> setRepresentative(
            @PathVariable UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        BikeResponse bike = bikeService.setRepresentative(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(bike));
    }
}
