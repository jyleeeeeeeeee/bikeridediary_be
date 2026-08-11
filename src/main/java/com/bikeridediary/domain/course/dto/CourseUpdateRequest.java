package com.bikeridediary.domain.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CourseUpdateRequest(
        // 코스 이름 (null이면 변경 없음)
        @Size(max = 200) String name,
        // 설명 (null이면 변경 없음)
        String description,
        // 공개 여부 (null이면 변경 없음)
        Boolean isPublic,
        // 변경된 waypoints (null이면 변경 없음, regeneratePath=true와 함께 와야 유효)
        @Valid List<WaypointRequest> waypoints,
        // waypoints 변경 시 path/distance도 함께 갱신할지 여부 (앱이 waypoint 편집 후 preview 재실행한 경우 true)
        boolean regeneratePath,
        // 옵션 B: regeneratePath=true 시 앱이 preview로 받은 신규 path (regeneratePath=false면 무시)
        String path,
        // 옵션 B: regeneratePath=true 시 앱이 preview로 받은 신규 총 거리 (regeneratePath=false면 무시)
        Integer distanceMeters,
        // 옵션 B: regeneratePath=true 시 앱이 preview로 받은 bbox JSON 문자열 (선택)
        String bbox
) {}