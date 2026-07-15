package com.bikeridediary.domain.place.controller;

import com.bikeridediary.domain.place.dto.*;
import com.bikeridediary.domain.place.service.PlaceService;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "장소", description = "라이더 큐레이션 POI (명소/카페/센터)")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @Operation(summary = "장소 목록 조회 (전체 또는 카테고리 필터)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> list(
            @Nullable @RequestParam("category") String category
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.list(category)));
    }

    @Operation(summary = "장소 좌표 변경")
    @PatchMapping("/{id}/coordinates")
    public ResponseEntity<ApiResponse<PlaceResponse>> updateCoordinates(
            @PathVariable("id") UUID id,
            @RequestBody CoordinateUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.updateCoordinates(id, request)));
    }

    @Operation(summary = "장소 정보 변경")
    @PatchMapping("/{id}/info")
    public ResponseEntity<ApiResponse<PlaceResponse>> updateInfo(
            @PathVariable("id") UUID id,
            @RequestBody PlaceInfoUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.updateInfo(id, request)));
    }

    @Operation(summary = "네이버 지역 검색")
    @GetMapping("/search-external")
    public ResponseEntity<ApiResponse<List<PlaceCandidateResponse>>> searchExternal(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.searchExternal(query)));
    }


    @Operation(summary = "네이버 지역 검색 결과 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<PlaceResponse>> addNewPlace(
            @RequestBody PlaceInsertRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        return ResponseEntity.ok(ApiResponse.ok(placeService.addNewPlace(request, userId)));
    }
}
