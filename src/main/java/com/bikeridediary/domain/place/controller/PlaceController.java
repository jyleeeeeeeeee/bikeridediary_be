package com.bikeridediary.domain.place.controller;

import com.bikeridediary.domain.place.dto.*;
import com.bikeridediary.domain.place.service.PlaceService;
import com.bikeridediary.domain.place.service.PlaceWishService;
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
    private final PlaceWishService placeWishService;

    @Operation(summary = "장소 목록 조회 (전체 또는 카테고리 필터)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> list(
            @Nullable @RequestParam("category") String category,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails != null ? userDetails.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.ok(placeService.list(category, userId)));
    }

    @Operation(summary = "네이버 지역 검색 (start=1부터, 페이지당 5건 상한)")
    @GetMapping("/search-external")
    public ResponseEntity<ApiResponse<List<PlaceCandidateResponse>>> searchExternal(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "1") int start
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.searchExternal(query, start)));
    }

    @Operation(summary = "주소 검색 (NCP Geocoding)")
    @GetMapping("/geocode")
    public ResponseEntity<ApiResponse<List<GeocodeResultResponse>>> geocode(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.geocode(query)));
    }

    @Operation(summary = "좌표 → 주소 변환 (NCP Reverse Geocoding)")
    @GetMapping("/reverse-geocode")
    public ResponseEntity<ApiResponse<String>> reverseGeocode(
            @RequestParam("lat") java.math.BigDecimal lat,
            @RequestParam("lng") java.math.BigDecimal lng
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.reverseGeocode(lat, lng)));
    }

    @Operation(summary = "장소 등록 순위 (유저별 등록 건수 내림차순)")
    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<List<PlaceRankingResponse>>> rankings() {
        return ResponseEntity.ok(ApiResponse.ok(placeService.getRankings()));
    }


    @Operation(summary = "장소 찜 추가")
    @PostMapping("/{placeId}/wish")
    public ResponseEntity<ApiResponse<PlaceResponse>> addWish(
            @PathVariable UUID placeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PlaceResponse placeResponse = placeWishService.addWish(placeId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(placeResponse));
    }

    @Operation(summary = "장소 찜 해제")
    @DeleteMapping("/{placeId}/wish")
    public ResponseEntity<ApiResponse<PlaceResponse>> removeWish(
            @PathVariable UUID placeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PlaceResponse placeResponse = placeWishService.removeWish(placeId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(placeResponse));
    }

    @Operation(summary = "내 찜 목록 조회")
    @GetMapping("/wishes/me")
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> myWishes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeWishService.listMyWishes(userDetails.getUserId())));
    }

}
