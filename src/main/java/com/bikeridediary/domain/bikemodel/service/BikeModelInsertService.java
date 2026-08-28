package com.bikeridediary.domain.bikemodel.service;

import com.bikeridediary.domain.bikemodel.entity.BikeModelEntity;
import com.bikeridediary.domain.bikemodel.entity.ManufacturerEntity;
import com.bikeridediary.domain.bikemodel.repository.BikeModelRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BikeModelInsertService {
    private final BikeModelRepository bikeModelRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBikeModelEntity(ManufacturerEntity manufacturer, JsonNode node) {
        bikeModelRepository.save(BikeModelEntity.create(
                manufacturer,
                node.path("model").asText(null),
                null,
                node.path("type").asText(null),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));
    }
}
