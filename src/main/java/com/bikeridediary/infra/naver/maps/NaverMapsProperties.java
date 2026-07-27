package com.bikeridediary.infra.naver.maps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.maps")
public record NaverMapsProperties(
        String clientId,
        String clientSecret,
        String directionUrl,
        String direction15Url,
        String geocodingUrl,
        String reverseGeocodingUrl
) {
}
