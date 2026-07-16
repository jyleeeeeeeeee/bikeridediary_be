package com.bikeridediary.domain.course.controller;

import com.bikeridediary.domain.course.dto.CourseDetailResponse;
import com.bikeridediary.domain.course.dto.CourseSummaryResponse;
import com.bikeridediary.domain.course.service.CourseService;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Tag(name = "코스", description = "라이딩 코스 조회 및 즐겨찾기 관리")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "MY탭 — 내가 만든 코스 + 즐겨찾기한 남의 코스")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getMyList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getMyList(userDetails.getUserId())));
    }

    @Operation(summary = "탐색탭 — 공개 코스 목록 (keyword로 코스명 검색 가능)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getPublicList(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails != null ? userDetails.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.ok(courseService.getPublicList(userId, keyword)));
    }

    @Operation(summary = "코스 상세 조회 (waypoints + path 포함)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getDetail(id, userDetails.getUserId())));
    }

    @Operation(summary = "즐겨찾기 등록")
    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Boolean>> addFavorite(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.addFavorite(id, userDetails.getUserId())));
    }

    @Operation(summary = "즐겨찾기 해제")
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Boolean>> removeFavorite(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.removeFavorite(id, userDetails.getUserId())));
    }

    @Operation(summary = "코스 삭제 (작성자만, hard delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        courseService.deleteCourse(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
