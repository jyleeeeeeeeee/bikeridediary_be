package com.bikeridediary.domain.place.controller;

import com.bikeridediary.domain.place.dto.*;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장소 변경 요청", description = "유저가 신규 등록/수정을 요청하고 어드민이 승인/거절한다")
@RestController
@RequestMapping("/api/v1/place-change-requests")
@RequiredArgsConstructor
public class PlaceChangeRequestController {

    private final PlaceChangeRequestService service;

    @Operation(summary = "요청 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<PlaceChangeRequestResponse>> create(
            @RequestBody PlaceChangeRequestCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.create(userDetails.getUserId(), req)));
    }

    @Operation(summary = "내 요청 목록 (페이징)")
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<PageResponse<PlaceChangeRequestResponse>>> listMine(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.listMine(userDetails.getUserId(), pageable)));
    }
}
