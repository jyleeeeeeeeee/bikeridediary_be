package com.bikeridediary.domain.station.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opinet")
public record OpinetProperties(
        String baseUrl,
        String apiKey
) {
}
