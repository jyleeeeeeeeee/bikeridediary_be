package com.bikeridediary.infra.naver.search;

import com.bikeridediary.infra.naver.search.dto.NaverLocalSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
@Component
public class NaverSearchClient {

    private final NaverSearchProperties properties;
    private final RestTemplate restTemplate;

    public NaverLocalSearchResponse search(String query, int start) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", properties.clientId());
        headers.set("X-NCP-APIGW-API-KEY", properties.clientSecret());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Naver 지역검색: display 최대 5 (스펙 상한), start는 1부터, 페이징 파라미터
        return restTemplate
                .exchange(
                        UriComponentsBuilder.fromUriString(properties.url())
                            .queryParam("query", query)
                            .queryParam("display", 5)
                            .queryParam("start", start)
                            .toUriString(),
                        HttpMethod.GET,
                        requestEntity,
                        NaverLocalSearchResponse.class)
                .getBody();
    }
}
