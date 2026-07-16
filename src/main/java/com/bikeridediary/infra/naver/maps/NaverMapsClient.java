package com.bikeridediary.infra.naver.maps;

import com.bikeridediary.infra.naver.search.dto.NaverLocalSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
public class NaverMapsClient {

    private final NaverMapsProperties properties;
    private final RestTemplate restTemplate;

    public NaverLocalSearchResponse search(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Naver-Client-Id", properties.clientId());
        headers.set("X-Naver-Client-Secret", properties.clientSecret());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromUriString(properties.url())
                .queryParam("query", query)
                .queryParam("display", 5)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toString();

        return restTemplate
                .exchange(url, HttpMethod.GET, requestEntity, NaverLocalSearchResponse.class)
                .getBody();
    }
}
