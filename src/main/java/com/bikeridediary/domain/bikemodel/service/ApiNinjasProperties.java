package com.bikeridediary.domain.bikemodel.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api-ninjas")
public record ApiNinjasProperties(
        String apiKey
) {
}
