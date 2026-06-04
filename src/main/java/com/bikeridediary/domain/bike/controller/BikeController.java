package com.bikeridediary.domain.bike.controller;

import com.bikeridediary.domain.bike.dto.BikeCreateRequest;
import com.bikeridediary.domain.bike.dto.BikeResponse;
import com.bikeridediary.domain.bike.dto.BikeUpdateRequest;
import com.bikeridediary.domain.bike.service.BikeService;
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

// 바이크 관리 API 컨트롤러
@RestController
@RequestMapping("/api/v1/bikes")
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    // 인증된 사용자의 모든 바이크 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<BikeResponse>>> getMyBikes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        List<BikeResponse> bikes = bikeService.getMyBikes(userId);
        return ResponseEntity.ok(ApiResponse.ok(bikes));
    }

    // 특정 바이크 상세 조회
    @GetMapping("/{bikeId}")
    public ResponseEntity<ApiResponse<BikeResponse>> getBike(
            @PathVariable UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        BikeResponse bike = bikeService.getBike(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(bike));
    }

    // 새로운 바이크 등록
    @PostMapping
    public ResponseEntity<ApiResponse<BikeResponse>> createBike(
            @Valid @RequestBody BikeCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        BikeResponse bike = bikeService.createBike(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(bike));
    }

    // 바이크 정보 수정
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

    // 바이크 삭제 (소프트 삭제)
    @DeleteMapping("/{bikeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBike(
            @PathVariable UUID bikeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        bikeService.deleteBike(bikeId, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 특정 바이크를 대표 바이크로 설정
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
