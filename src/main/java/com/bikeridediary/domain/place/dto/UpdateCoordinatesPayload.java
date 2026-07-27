package com.bikeridediary.domain.place.dto;

import java.math.BigDecimal;

public record UpdateCoordinatesPayload(
        BigDecimal latitude,
        BigDecimal longitude
) {}