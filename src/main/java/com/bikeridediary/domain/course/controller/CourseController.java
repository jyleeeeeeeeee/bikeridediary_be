package com.bikeridediary.domain.course.controller;

import com.bikeridediary.domain.course.dto.*;
import com.bikeridediary.domain.course.service.CourseService;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.response.ApiResponse;
import com.bikeridediary.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Tag(name = "코스", description = "라이딩 코스 조회 및 즐겨찾기 관리")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "내가 만든 코스 (페이징)")
    @GetMapping("/my/owned")
    public ResponseEntity<ApiResponse<PageResponse<CourseSummaryResponse>>> getMyOwned(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                courseService.getMyOwnedCourses(userDetails.getUserId(), pageable)));
    }

    @Operation(summary = "내가 즐겨찾기한 코스 (페이징)")
    @GetMapping("/my/favorites")
    public ResponseEntity<ApiResponse<PageResponse<CourseSummaryResponse>>> getMyFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // 즐겨찾기 등록순은 JOIN 테이블의 f.createdAt이라 Pageable sort로 지정 어려움.
            // 코스 자체의 createdAt 기준으로 정렬 (충분히 자연스러움).
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                courseService.getMyFavoriteCourses(userDetails.getUserId(), pageable)));
    }

    @Operation(summary = "탐색탭 — 공개 코스 목록 (keyword로 코스명 검색 가능, 페이징)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseSummaryResponse>>> getPublicList(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        UUID userId = userDetails != null ? userDetails.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.ok(
                courseService.getPublicList(userId, keyword, pageable)));
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

    @Operation(summary = "코스 생성 (Directions API 경로 계산 포함)")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseDetailResponse>> createCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CourseCreateRequest request
            ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        courseService.createCourse(userDetails.getUserId(), request)
                ));
    }

    @Operation(summary = "코스 수정 (작성자만, regeneratePath=true 시 Directions 재호출)")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> updateCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CourseUpdateRequest request
            ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.updateCourse(id, userDetails.getUserId(), request)));
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

    @Operation(summary = "경로 미리보기 (저장 없이 Directions 호출, path + distance + bbox 반환)")
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<CoursePreviewResponse>> previewCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CoursePreviewRequest request
            ) {
        // userDetails는 인증 강제용 (SecurityConfig가 permitAll 아님을 명시)
        return ResponseEntity.ok(ApiResponse.ok(courseService.previewCourse(request)));
    }
}
