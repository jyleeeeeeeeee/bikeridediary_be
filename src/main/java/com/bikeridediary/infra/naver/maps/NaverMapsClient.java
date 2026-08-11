package com.bikeridediary.infra.naver.maps;

import com.bikeridediary.global.exception.BusinessException;
import static com.bikeridediary.global.exception.ErrorCode.*;
import com.bikeridediary.infra.naver.maps.dto.NaverDirectionsResponse;
import com.bikeridediary.infra.naver.maps.dto.NaverGeocodeResponse;
import com.bikeridediary.infra.naver.maps.dto.NaverReverseGeocodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;



@Slf4j
@RequiredArgsConstructor
@Component
public class NaverMapsClient {

    private static final String API_KEY_ID = "X-NCP-APIGW-API-KEY-ID";
    private static final String API_KEY = "X-NCP-APIGW-API-KEY";

    private final NaverMapsProperties properties;
    private final RestTemplate restTemplate;

    /**
     * NCP Geocoding API 호출 — 주소/장소 텍스트를 좌표로 변환.
     *
     * @param query 검색어 (예: "서울시 강남구 역삼동", "롯데월드타워")
     *              한글 그대로 전달 가능 (RestTemplate이 URL 인코딩 처리).
     * @return status, addresses(도로명/지번/x=경도/y=위도), meta_totalCount 등
     *         결과 없으면 addresses가 빈 배열. 최대 15건 (count=15 고정).
     */
    public NaverGeocodeResponse search(String query) {
        String url = properties.geocodingUrl() + "?query=" + query + "&count=15";
        return executeGetRequest(url, NaverGeocodeResponse.class);
    }

    /**
     * NCP Reverse Geocoding API 호출 — 좌표를 주소로 변환.
     * orders=roadaddr,addr 로 도로명+지번 모두 요청 (results 배열에 순서대로).
     *
     * @param latitude  위도 (WGS84)
     * @param longitude 경도 (WGS84)
     */
    public NaverReverseGeocodeResponse reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.reverseGeocodingUrl())
                .queryParam("coords", longitude.toPlainString() + "," + latitude.toPlainString())
                .queryParam("orders", "roadaddr,addr")
                .queryParam("output", "json")
                .build(false)
                .toUriString();
        return executeGetRequest(url, NaverReverseGeocodeResponse.class);
    }

    /**
     * Naver Directions 5, Directions 15 API 호출.
     *
     * @param startLng  출발지 경도
     * @param startLat  출발지 위도
     * @param goalLng   도착지 경도
     * @param goalLat   도착지 위도
     * @param waypoints 경유지 목록 (최대 15개, START/GOAL 제외 VIA만). 비어있으면 waypoints 파라미터 생략.
     * @return 파싱된 응답 DTO
     * @throws BusinessException COURSE_DIRECTIONS_FAILED 또는 COURSE_DIRECTIONS_WAYPOINTS_LIMIT
     */
    public NaverDirectionsResponse directions(
            BigDecimal startLng, BigDecimal startLat,
            BigDecimal goalLng, BigDecimal goalLat,
            List<Waypoint> waypoints) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(waypoints.size() <= 5 ? properties.directionUrl() : properties.direction15Url())
                .queryParam("start", startLng.toPlainString() + "," + startLat.toPlainString())
                .queryParam("goal", goalLng.toPlainString() + "," + goalLat.toPlainString())
                .queryParam("cartype", 1)
                .queryParam("option", "traavoidcaronly");

        if(!waypoints.isEmpty()) {
            String waypointStr = waypoints.stream()
                    .map(w -> w.longitude.toPlainString() + "," + w.latitude.toPlainString())
                    .collect(Collectors.joining("|"));
            builder.queryParam("waypoints", waypointStr);
        }
        String url = builder.build(false).toUriString();
        NaverDirectionsResponse response;
        try {
            response = executeGetRequest(url, NaverDirectionsResponse.class);
        } catch (RestClientException e) {
            log.error("[NaverDirections] HTTP 호출 실패: {}", e.getMessage());
            throw new BusinessException(COURSE_DIRECTIONS_FAILED);
        }

        if (response == null || response.code() != 0) {
            int code = response == null ? -1 : response.code();
            String msg = response == null ? "null response" : response.message();
            log.error("[NaverDirections] code={}, message={}", code, msg);
            throw new BusinessException(COURSE_DIRECTIONS_FAILED);
        }

        if (response.route() == null
                || response.route().traavoidcaronly() == null
                || response.route().traavoidcaronly().isEmpty()) {
            log.error("[NaverDirections] traavoidcaronly 경로 없음");
            throw new BusinessException(COURSE_DIRECTIONS_FAILED);
        }

        return response;
    }


    /**
     * 경유지 좌표 wrapper — VIA waypoint에서 좌표만 추출해서 넘길 때 사용.
     */
    public record Waypoint(BigDecimal latitude, BigDecimal longitude) {}

    /**
     * NCP GET 요청 공통 실행 메서드
     */
    private <T> T executeGetRequest(String url, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(API_KEY_ID, properties.clientId());
        headers.set(API_KEY, properties.clientSecret());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType).getBody();
    }
}