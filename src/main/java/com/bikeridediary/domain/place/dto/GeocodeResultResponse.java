package com.bikeridediary.domain.place.dto;

import com.bikeridediary.infra.naver.maps.dto.NaverGeocodeResponse;
import com.bikeridediary.infra.naver.maps.dto.NaverGeocodeResponse.Address;

import java.math.BigDecimal;

public record GeocodeResultResponse(
        String roadAddress, // 도로명 주소
        String jibunAddress, // 지번 주소
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static GeocodeResultResponse from(Address addr) {
        return new GeocodeResultResponse(
                addr.roadAddress() == null || addr.jibunAddress().isBlank() ?
                        null : addr.roadAddress(),
                addr.jibunAddress(),
                new BigDecimal(addr.y()).setScale(7, java.math.RoundingMode.HALF_UP),
                new BigDecimal(addr.x()).setScale(7, java.math.RoundingMode.HALF_UP)
        );
    }
}
