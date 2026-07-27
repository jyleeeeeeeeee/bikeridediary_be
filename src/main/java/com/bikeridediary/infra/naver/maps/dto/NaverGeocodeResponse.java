package com.bikeridediary.infra.naver.maps.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverGeocodeResponse(
        String status,
        List<Address> addresses,
        int meta_totalCount,
        String errorMessage
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
            String roadAddress, // 도로명 주소
            String jibunAddress, // 지번 주소
            String x, // X 좌표(경도)
            String y // Y 좌표(위도)
    ) {

    }
}
