package com.bikeridediary.infra.naver.search;

import com.bikeridediary.global.logging.ApiNames;
import com.bikeridediary.global.logging.LogExternalApi;
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


    @LogExternalApi(apiName = ApiNames.NAVER_SEARCH)
    public NaverLocalSearchResponse search(String query, int start) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", properties.clientId());
        headers.set("X-NCP-APIGW-API-KEY", properties.clientSecret());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Naver 지역검색: display 최대 5 (스펙 상한), start는 1부터, 페이징 파라미터
        String url = properties.url()
                + "?query=" + query
                + "&display=5"
                + "&start=" + start;

        return restTemplate
                .exchange(url, HttpMethod.GET, requestEntity, NaverLocalSearchResponse.class)
                .getBody();
    }
}
