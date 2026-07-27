package com.bikeridediary.domain.place.dto;

import java.math.BigDecimal;
import java.util.UUID;

// CREATE 요청 payload (앱이 생성한 clientUuid를 승인 시 places.id로 그대로 사용)
public record CreatePlaceRequestPayload(
        UUID clientUuid,
        String placeName,
        String category,        // FAMOUS/CAFE/RESTAURANT/SERVICE/OTHER
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String roadAddress,
        String description,
        String phone,
        String photoUrl
) {}