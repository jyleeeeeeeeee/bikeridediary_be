package com.bikeridediary.domain.weather;

import com.bikeridediary.domain.station.dto.AvgOil;
import com.bikeridediary.domain.station.dto.OpinetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/weathers")
public class WeatherController {

    private final WeatherService weatherService;
    @GetMapping("/currentConditions")
    public Object getCurrentConditions(double lat, double lng) {
        return weatherService.getCurrentConditions(lat, lng);
    }
}
