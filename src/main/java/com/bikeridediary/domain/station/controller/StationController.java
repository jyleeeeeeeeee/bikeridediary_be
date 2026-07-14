package com.bikeridediary.domain.station.controller;

import com.bikeridediary.domain.station.dto.AvgOil;
import com.bikeridediary.domain.station.dto.StationOil;
import com.bikeridediary.domain.station.service.StationService;
import com.bikeridediary.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService client;

    @GetMapping("/avg")
    public ResponseEntity<ApiResponse<List<AvgOil>>> getAvgAllPrice() {
        List<AvgOil> response = client.getAvgAllPrice();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<StationOil>>> getNearby(double lat, double lng, int radius, int sort, String prodcd) {
        List<StationOil> response = client.getNearby(lat, lng, radius, sort, prodcd);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}
