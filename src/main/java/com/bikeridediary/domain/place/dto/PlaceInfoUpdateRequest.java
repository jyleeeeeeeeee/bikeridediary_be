package com.bikeridediary.domain.place.dto;

import java.math.BigDecimal;

public record PlaceInfoUpdateRequest(
        String placeName,
        String category // FAMOUS, CAFE, RESTAURANT, SERVICE
){
}
