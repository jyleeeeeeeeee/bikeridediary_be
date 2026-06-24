package com.bikeridediary.domain.station.controller;

import com.bikeridediary.domain.station.dto.OilResponse;
import com.bikeridediary.infra.opinet.OpinetClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stations")
public class StationController {

    private final OpinetClient client;

    @GetMapping("/avg")
    public List<OilResponse.Oil> getAvgAllPrice() {
        return client.getAvgAllPrice();
    }

    @GetMapping("/around")
    public List<OilResponse.Oil> getAvgAllPrice() {
        return client.getAvgAllPrice();
    }

}
