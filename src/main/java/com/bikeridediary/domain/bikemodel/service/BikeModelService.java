package com.bikeridediary.domain.bikemodel.service;

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
    public List<String> getModelNames(Long manufacturerId) {
        return bikeModelRepository.findDistinctModelNamesByManufacturerId(manufacturerId);
    }

    @Transactional(readOnly = true)
    public List<BikeModelResponse> getModelDetails(Long manufacturerId, String modelName) {
        return bikeModelRepository.findByManufacturerIdAndName(manufacturerId, modelName)
                .stream()
                .map(BikeModelResponse::from)
                .toList();
    }

    // ── 동기화 API (관리자용) ────────────────────────────────

    @Transactional
    public int syncManufacturerModels(Long manufacturerId) {
        ManufacturerEntity manufacturer = manufacturerRepository.findById(manufacturerId)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found: " + manufacturerId));

        return syncFromApi(manufacturer);
    }

    @Transactional
    public int syncAllManufacturers() {
        List<ManufacturerEntity> manufacturers = manufacturerRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        int totalSynced = 0;
        for (ManufacturerEntity manufacturer : manufacturers) {
            try {
                totalSynced += syncFromApi(manufacturer);
                Thread.sleep(1100); // API rate limit (1 req/sec on free plan)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Failed to sync manufacturer: {}", manufacturer.getApiName(), e);
            }
        }
        return totalSynced;
    }

    private int syncFromApi(ManufacturerEntity manufacturer) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        int totalSaved = 0;
        int offset = 0;

        while (true) {
            String url = API_BASE_URL + "?make=" + manufacturer.getApiName().replace(" ", "%20") + "&offset=" + offset;

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
                JsonNode models = objectMapper.readTree(response.getBody());

                if (!models.isArray() || models.isEmpty()) break;

                for (JsonNode model : models) {
                    String name = model.path("model").asText(null);
                    Integer year = parseYear(model.path("year").asText(null));

                    if (name == null || name.isBlank()) continue;
                    if (bikeModelRepository.existsByManufacturerIdAndNameAndYear(manufacturer.getId(), name, year)) continue;

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

                if (models.size() < 30) break;
                offset += 30;

                Thread.sleep(1100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("API call failed for {} at offset {}", manufacturer.getApiName(), offset, e);
                break;
            }
        }

        log.info("Synced {} models for {}", totalSaved, manufacturer.getApiName());
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
}
