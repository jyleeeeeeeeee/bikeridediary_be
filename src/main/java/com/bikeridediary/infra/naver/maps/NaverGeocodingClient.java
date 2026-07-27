package com.bikeridediary.infra.naver.maps;

import com.bikeridediary.infra.naver.maps.dto.NaverGeocodeResponse;
import com.bikeridediary.infra.naver.search.dto.NaverLocalSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class NaverGeocodingClient {

    private final NaverMapsProperties properties;
    private final RestTemplate restTemplate;

    public NaverGeocodeResponse search(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", properties.clientId());
        headers.set("X-NCP-APIGW-API-KEY", properties.clientSecret());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = properties.geocodingUrl() + "?query=" + query + "&count=15";
        return restTemplate
                .exchange(url, HttpMethod.GET, requestEntity, NaverGeocodeResponse.class)
                .getBody();
    }
}