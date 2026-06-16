package com.bikeridediary.domain.fueling.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 연료 종류 정의
@Getter
@RequiredArgsConstructor
public enum FuelType {

    REGULAR("REGULAR", "일반유"),
    PREMIUM("PREMIUM", "고급유"),
    DIESEL("DIESEL", "경유");

    private final String code;
    private final String displayName;
}
