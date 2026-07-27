package com.bikeridediary.domain.place.repository;

import java.util.UUID;

// 유저별 장소 등록 건수 집계 결과
  public interface PlaceRegistrationCount {
      UUID getUserId();
      String getNickname();
      long getCount();

  }

