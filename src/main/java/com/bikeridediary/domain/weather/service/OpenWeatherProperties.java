package com.bikeridediary.domain.weather.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openweather")
public record OpenWeatherProperties(
        String baseUrl,
        String apiKey
) {
}
