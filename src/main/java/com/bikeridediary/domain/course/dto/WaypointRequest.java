package com.bikeridediary.domain.course.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * waypoint 하나의 입력 DTO.
 * role: "START" | "VIA" | "GOAL" — DB CHECK 제약, Naver Directions API의 goal 파라미터와 통일.
 * placeId: 등록된 place에서 선택한 경우 UUID, 임의 지점은 null.
 */
public record WaypointRequest (

    // 역할 (START/VIA/GOAL)
    @NotBlank String role,
    // 순서 인덱스 (0-based, 앱이 지정)
    short seq,
    // 위도 (소수점 7자리 이내)
    @NotNull
    BigDecimal latitude,
    // 경도 (소수점 7자리 이내)
    @NotNull BigDecimal longitude,
    // 지점 이름 (place 선택 시 place.placeName, 임의 지점은 사용자 입력 또는 주소)
    String placeName,
    // 등록된 place ID (임의 지점은 null)
    UUID placeId,
    // place 카테고리 코드 (앱 마커 아이콘용, null 가능)
    String placeCategoryCode
){ }
