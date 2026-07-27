package com.bikeridediary.domain.place.dto;

import com.bikeridediary.domain.place.entity.PlaceChangeRequestType;

import java.util.Map;
import java.util.UUID;

// 클라이언트가 요청 생성 시 보내는 DTO.
// payload는 type별로 다른 필드셋을 담는 map 형태로 받고, Service에서 type별 검증.
public record PlaceChangeRequestCreateRequest(
        PlaceChangeRequestType type,
        UUID targetPlaceId,          // CREATE는 null, UPDATE_*는 필수
        Map<String, Object> payload
) {}