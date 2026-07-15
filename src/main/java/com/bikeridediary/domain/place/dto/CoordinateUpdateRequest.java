package com.bikeridediary.domain.place.dto;

import java.math.BigDecimal;

public record CoordinateUpdateRequest (
        BigDecimal latitude,
        BigDecimal longitude
){
}
