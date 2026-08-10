package com.bikeridediary.domain.course.dto;

import java.util.List;

public record CoursePreviewResponse(
        // 경로 좌표 JSON 문자열 [[lng,lat],...] — 앱이 폴리라인으로 표시
        String path,
        // 총 거리 (미터)
        int distanceMeters,
        // 전체 경로 경계 영역 [[minLng,minLat],[maxLng,maxLat]] — 앱 지도 fitBounds용
        List<List<Double>> bbox
) {}