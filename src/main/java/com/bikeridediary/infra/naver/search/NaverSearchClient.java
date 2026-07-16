package com.bikeridediary.infra.naver.search;

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
public class NaverSearchClient {

    private final NaverSearchProperties properties;
    private final RestTemplate restTemplate;

    public NaverLocalSearchResponse search(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Naver-Client-Id", properties.clientId());
        headers.set("X-Naver-Client-Secret", properties.clientSecret());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = properties.url() + "?query=" + query + "&display=5";
        return restTemplate
                .exchange(url, HttpMethod.GET, requestEntity, NaverLocalSearchResponse.class)
                .getBody();
    }
}
