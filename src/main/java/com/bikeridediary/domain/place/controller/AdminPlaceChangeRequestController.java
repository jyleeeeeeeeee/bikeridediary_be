package com.bikeridediary.domain.place.controller;

import com.bikeridediary.domain.place.dto.AdminPlaceChangeRequestResponse;
import com.bikeridediary.domain.place.dto.AdminReviewRequest;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestStatus;
import com.bikeridediary.domain.place.service.PlaceChangeRequestService;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.response.ApiResponse;
import com.bikeridediary.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "어드민 - 장소 변경 요청", description = "어드민만 접근 가능")
@RestController
@RequestMapping("/api/v1/admin/place-change-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlaceChangeRequestController {

    private final PlaceChangeRequestService service;

    @Operation(summary = "요청 목록 (상태 필터, 기본 PENDING, 페이징)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminPlaceChangeRequestResponse>>> list(
            @Nullable @RequestParam("status") PlaceChangeRequestStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(service.listForAdmin(status, pageable)));
    }

    @Operation(summary = "요청 상세")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminPlaceChangeRequestResponse>> infoForAdmin(
            @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(service.infoForAdmin(id)));
    }

    @Operation(summary = "요청 승인")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AdminPlaceChangeRequestResponse>> approve(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) AdminReviewRequest review,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.approve(id, userDetails.getUserId(), review)));
    }

    @Operation(summary = "요청 거절")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AdminPlaceChangeRequestResponse>> reject(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) AdminReviewRequest review,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.reject(id, userDetails.getUserId(), review)));
    }
}