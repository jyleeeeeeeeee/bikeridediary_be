package com.bikeridediary.infra.naver.search.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverLocalItem(
    String title,
    String link,
    String category,
    String description,
    String telephone,
    String address,
    String roadAddress,
    String mapx,
    String mapy
) {
}
