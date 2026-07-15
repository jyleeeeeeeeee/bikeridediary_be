package com.bikeridediary.domain.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherService {

    private final OpenWeatherProperties properties;
    private final RestTemplate restTemplate;

    public Object getCurrentConditions(double lat, double lng) {
        try {
            String url = properties.baseUrl() + "/weather"
                    + "?appid=" + properties.apiKey()
                    + "&lat=" + lat
                    + "&lon=" + lng;
            java.util.Map body = restTemplate.exchange(url, HttpMethod.GET, null, java.util.Map.class).getBody();
            return body;
        } catch (Exception e) {
            return null;
        }
    }
}
