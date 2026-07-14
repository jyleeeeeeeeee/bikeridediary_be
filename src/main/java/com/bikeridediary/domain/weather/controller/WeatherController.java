package com.bikeridediary.domain.weather.controller;

import com.bikeridediary.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
