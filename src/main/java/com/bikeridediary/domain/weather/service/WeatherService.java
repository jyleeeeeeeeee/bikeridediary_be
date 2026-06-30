package com.bikeridediary.domain.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherService {

    @Value("${openweather.base-url}")
    private String OPENWEATHER_BASE_URL;

    @Value("${openweather.api-key}")
    private String OPENWEATHER_API_KEY;

    private final RestTemplate restTemplate;

    public Object getCurrentConditions(double lat, double lng) {
        try {
            String url = OPENWEATHER_BASE_URL + "/currentConditions:lookup"
                    + "?key=" + OPENWEATHER_API_KEY
                    + "&location.latitude=" + lat
                    + "&location.longitude=" + lng;
            java.util.Map body = restTemplate.exchange(url, HttpMethod.GET, null, java.util.Map.class).getBody();
            return body;
        } catch (Exception e) {
            return null;
        }
    }
}
