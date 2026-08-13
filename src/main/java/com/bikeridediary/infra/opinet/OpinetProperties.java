package com.bikeridediary.infra.opinet;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opinet")
public record OpinetProperties(
        String baseUrl,
        String apiKey
) {
}
