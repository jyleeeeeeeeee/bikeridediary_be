package com.bikeridediary.domain.place.dto;

import java.math.BigDecimal;

public record PlaceCandidateResponse(
      String name,             // HTML 태그 제거된 title
      String naverCategory,
      BigDecimal latitude,
      BigDecimal longitude,
      String address,
      String roadAddress
  ) {}
