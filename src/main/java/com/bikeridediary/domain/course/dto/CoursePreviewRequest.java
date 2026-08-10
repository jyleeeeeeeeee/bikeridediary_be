package com.bikeridediary.domain.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CoursePreviewRequest(
        // waypoints (START + GOAL 최소 2개)
        @NotEmpty @Valid List<WaypointRequest> waypoints
) {}