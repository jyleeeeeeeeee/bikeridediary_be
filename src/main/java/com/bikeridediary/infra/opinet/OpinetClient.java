package com.bikeridediary.infra.opinet;

import com.bikeridediary.domain.station.dto.OilResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpinetClient {
    @Value("${opinet.base-url}")
    private String OPINET_BASE_URL;

    @Value("${opinet.api-key}")
    private String code;
    private final CoodinateConverter converter;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<OilResponse.Oil> getAvgAllPrice() {
        try {
            String url = OPINET_BASE_URL + "/avgAllPrice.do?out=json&code=" + code;
            OilResponse oilResponse = restTemplate.getForObject(url, OilResponse.class);
            assert oilResponse != null;
            return oilResponse.result().oil().stream().filter(oil -> oil.prodnm().contains("휘발유")).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("오피넷 유가 정보 조회 실패", e);
        }
        return List.of();
    }

}
