package com.bikeridediary.infra.naver.maps.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverDirectionsResponse(
        int code,
        String message,
        Route route
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(
            // option=traavoidcaronly 응답 (자동차 전용, 고속도로 제외)
            List<RoutePath> traavoidcaronly
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoutePath(
            // 요약 정보 (거리, 시간 등)
            Summary summary,
            // 경로 좌표 배열 [[lng, lat], [lng, lat], ...] — 수천 개 가능
            @JsonProperty("path")
            List<List<Double>> path
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(
            // 총 거리 (미터 단위, int)
            int distance,
            // 총 소요 시간 (밀리초, D2=A로 현재 미저장 — 필요 시 컬럼 추가 후 사용)
            int duration,
            @JsonProperty("bbox")
            List<List<Double>> bbox
    ) {}
}
