package com.bikeridediary.domain.bikemodel.service;

import com.bikeridediary.domain.bikemodel.dto.BikeModelNameResponse;
import com.bikeridediary.domain.bikemodel.dto.BikeModelResponse;
import com.bikeridediary.domain.bikemodel.dto.ManufacturerResponse;
import com.bikeridediary.domain.bikemodel.entity.BikeModelEntity;
import com.bikeridediary.domain.bikemodel.entity.ManufacturerEntity;
import com.bikeridediary.domain.bikemodel.repository.BikeModelRepository;
import com.bikeridediary.domain.bikemodel.repository.ManufacturerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BikeModelService {

    private final ManufacturerRepository manufacturerRepository;
    private final BikeModelRepository bikeModelRepository;
    private final ObjectMapper objectMapper;

    @Value("${api-ninjas.api-key:}")
    private String apiKey;

    private static final String API_BASE_URL = "https://api.api-ninjas.com/v1/motorcycles";

    // ── 조회 API ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ManufacturerResponse> getActiveManufacturers() {
        return manufacturerRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(ManufacturerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BikeModelNameResponse> getModelNames(String manufacturerName) {
        return bikeModelRepository.findDistinctModelNamesWithTypeByManufacturerName(manufacturerName);
    }

    @Transactional(readOnly = true)
    public List<BikeModelResponse> getModelDetails(String manufacturerName, String modelName) {
        return bikeModelRepository.findByManufacturerNameAndModelName(manufacturerName, modelName)
                .stream()
                .map(BikeModelResponse::from)
                .toList();
    }


    @Transactional
    public int syncManufacturerModels(String manufacturerName, String modelName) {
        ManufacturerEntity manufacturer = manufacturerRepository.findById(manufacturerName)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found: " + manufacturerName));

        return syncFromApi(manufacturer, modelName);
    }

    // ── 동기화 API (관리자용) ────────────────────────────────

    @Transactional
    public int syncManufacturerModels(String manufacturerName) {
        ManufacturerEntity manufacturer = manufacturerRepository.findById(manufacturerName)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found: " + manufacturerName));

        return syncFromApi(manufacturer, null);
    }

    @Transactional
    public int syncAllManufacturers() {
        List<ManufacturerEntity> manufacturers = manufacturerRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        int totalSynced = 0;
        for (ManufacturerEntity manufacturer : manufacturers) {
            try {
                totalSynced += syncFromApi(manufacturer, null);
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Failed to sync manufacturer: {}", manufacturer.getManufacturerName(), e);
            }
        }
        return totalSynced;
    }

    private int syncFromApi(ManufacturerEntity manufacturer, String modelName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        int totalSaved = 0;
        String url = API_BASE_URL + "?make=" + replaceSpace(manufacturer.getManufacturerName());
        if(modelName != null && !modelName.isEmpty()) {
            url = url + "&model=" + replaceSpace(modelName);
        }

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            JsonNode models = objectMapper.readTree(response.getBody());

            if (!models.isArray() || models.isEmpty()) return 0;

            for (JsonNode model : models) {
                String name = model.path("model").asText(null);
                Integer year = parseYear(model.path("year").asText(null));

                if (name == null || name.isBlank()) continue;
                name = name.trim();
                if (bikeModelRepository.existsByManufacturerManufacturerName(
                        manufacturer.getManufacturerName())) continue;

                BikeModelEntity entity = BikeModelEntity.create(
                        manufacturer,
                        name,
                        year,
                        model.path("type").asText(null),
                        model.path("displacement").asText(null),
                        model.path("engine").asText(null),
                        model.path("power").asText(null),
                        model.path("torque").asText(null),
                        model.path("total_weight").asText(null),
                        model.path("seat_height").asText(null),
                        model.path("fuel_capacity").asText(null)
                );
                bikeModelRepository.save(entity);
                totalSaved++;
            }
        } catch (Exception e) {
            log.error("API call failed for {}", manufacturer.getManufacturerName(), e);
        }

        log.info("Synced {} models for {}", totalSaved, manufacturer.getManufacturerName());
        return totalSaved;
    }

    private Integer parseYear(String yearStr) {
        if (yearStr == null || yearStr.isBlank()) return null;
        try {
            return Integer.parseInt(yearStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String replaceSpace(String str) {
        return str.replace(" ", "%20");
    }
}
