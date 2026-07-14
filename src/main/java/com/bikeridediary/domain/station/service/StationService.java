package com.bikeridediary.domain.station.service;

import com.bikeridediary.domain.station.dto.AvgOil;
import com.bikeridediary.domain.station.dto.StationOil;
import com.bikeridediary.domain.station.dto.OpinetResponse;
import com.bikeridediary.infra.coordinates.CoodinateConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StationService {
    @Value("${opinet.base-url}")
    private String OPINET_BASE_URL;

    @Value("${opinet.api-key}")
    private String code;
    private final CoodinateConverter converter;
    private final RestTemplate restTemplate;

    public List<AvgOil> getAvgAllPrice() {
        try {
            String url = OPINET_BASE_URL + "/avgAllPrice.do?out=json&code=" + code;
            OpinetResponse<AvgOil> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<OpinetResponse<AvgOil>>() {
                    }
            ).getBody();

            if (response == null) return List.of();
            return response.getResult().getOils().stream()
                    .filter(oil -> oil.getProdnm().contains("휘발유")).toList();
        } catch (Exception e) {
            log.error("오피넷 유가 정보 조회 실패", e);
            return List.of();
        }
    }

    public List<StationOil> getNearby(double lat, double lng, int radius, int sort, String prodcd) {
        try {
            double[] katec = converter.toKatec(lat, lng);
            String url = OPINET_BASE_URL + "/aroundAll.do?out=json&code=" + code
                    + "&x=" + katec[0] + "&y=" + katec[1] + "&radius=" + radius
                    + "&sort=" + sort + "&prodcd=" + prodcd;
            OpinetResponse<StationOil> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<OpinetResponse<StationOil>>() {
                    }
            ).getBody();

            if (response == null) return List.of();
            return response.getResult().getOils().stream()
                    .map(station -> {
                        double[] wgs84 = converter.toWgs84(station.getGisXCoor(), station.getGisYCoor());
                        station.setGisXCoor(wgs84[0]);
                        station.setGisYCoor(wgs84[1]);
                        return station;
                    }).toList();
        } catch (Exception e) {
            log.error("오피넷 유가 정보 조회 실패", e);
            return List.of();
        }
    }

}
