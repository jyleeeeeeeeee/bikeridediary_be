package com.bikeridediary.domain.place.controller;

import com.bikeridediary.domain.place.service.PlaceService;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "어드민 - 장소", description = "어드민만 접근 가능. 장소 삭제(soft/hard).")
@RestController
@RequestMapping("/api/v1/admin/places")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlaceController {

    private final PlaceService placeService;

    // mode=soft: places.deleted_at 세팅 (지도에서 숨김, 복구 가능)
    // mode=hard: places 완전 삭제. place_wishes/place_change_requests CASCADE로 함께 삭제됨
    @Operation(summary = "장소 삭제 (mode: soft|hard, 기본 soft)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") UUID id,
            @RequestParam(name = "mode", defaultValue = "soft") String mode
    ) {
        if ("hard".equalsIgnoreCase(mode)) {
            placeService.hardDeleteByAdmin(id);
        } else {
            placeService.softDeleteByAdmin(id);
        }
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
