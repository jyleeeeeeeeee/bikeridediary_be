package com.bikeridediary.domain.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CourseCreateRequest(
        // 코스 이름
        @NotBlank @Size(max = 200) String name,
        // 설명 (선택)
        String description,
        // 공개 여부
        boolean isPublic,
        // waypoints (START 1개 + GOAL 1개 + VIA 0~15개, seq는 앱이 재부여한 0-based 연속값)
        @NotEmpty @Valid List<WaypointRequest> waypoints,
        // 복사 편집 시 원본 코스 ID (신규 생성은 null)
        UUID sourceCourseId,
        // 옵션 B: preview에서 받은 경로 JSON 문자열 [[lng,lat],...] — 앱이 로컬 보관 후 저장 시 재전송
        @NotBlank String path,
        // 옵션 B: preview에서 받은 총 거리 (미터)
        @NotNull Integer distanceMeters,
        // 옵션 B: preview에서 받은 bbox JSON 문자열 [[minLng,minLat],[maxLng,maxLat]] (선택, 없으면 null)
        String bbox
) {}