package com.bikeridediary.domain.place.dto;

import java.math.BigDecimal;

public record PlaceInsertRequest(
        String placeName,
        String category,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String roadAddress,
        String description,
        String photoUrl,
        String phone) {

}
