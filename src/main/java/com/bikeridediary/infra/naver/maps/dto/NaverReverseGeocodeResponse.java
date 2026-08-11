package com.bikeridediary.infra.naver.maps.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * NCP Reverse Geocoding v2 응답 DTO.
 * 좌표(lat, lng) → 주소 문자열 조합에 필요한 필드만 매핑.
 * 요청 시 orders=roadaddr,addr 로 요청하면 results가 최대 2개(도로명/지번).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverReverseGeocodeResponse(
        Status status,
        List<Result> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(int code, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            // "roadaddr" (도로명) 또는 "addr" (지번)
            String name,
            Region region,
            Land land
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Region(
            Area area1, // 시/도
            Area area2, // 시/군/구
            Area area3, // 읍/면/동
            Area area4  // 리 (선택)
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Area(String name) {}

    /**
     * 도로명(roadaddr): name = 도로명, number1 = 본번, number2 = 부번, addition0.value = 건물명
     * 지번(addr):     number1 = 본번, number2 = 부번
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Land(
            String name,
            String number1,
            String number2,
            Addition addition0
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Addition(String value) {}
}
