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
        String displacement = node.path("displacement").asText(null);

        String engine = node.path("engine").asText(null);
        engine = engine == null ? null : translateEngine(engine);

        String fuelCapacity = node.path("fuel_capacity").asText(null);
        fuelCapacity = fuelCapacity == null ? null : fuelCapacity.substring(0, fuelCapacity.indexOf(" lit"));


        String power = node.path("power").asText(null);
        power = power == null ? null : power.substring(0, power.indexOf("HP")) + "마력";

        String torque = node.path("torque").asText(null);

        String totalWeight = node.path("total_weight").asText(null);
        totalWeight = totalWeight == null ? null : totalWeight.substring(0, totalWeight.indexOf("kg") + 2);

        String seatHeight = node.path("seat_height").asText(null);
        seatHeight = seatHeight == null ? null : seatHeight.substring(0, seatHeight.indexOf("mm") + 2);

        bikeModelRepository.save(BikeModelEntity.create(
                manufacturer,
                node.path("model").asText(null),
                null,
                node.path("type").asText(null),
                displacement.substring(0, displacement.indexOf("ccm") + 2),
                engine,
                power,
                torque,
                totalWeight,
                seatHeight,
                fuelCapacity
        ));
    }

    private String translateEngine(String engine) {
        return engine.replace("Single cylinder, ", "단기통, ")
                .replace("Twin, ", "2기통")
                .replace("In-line, ", "직렬")
                .replace("three, ", "3기통")
                .replace("four, ", "4기통")
                .replace("four-stroke", "4행정")
                .replace("two-stroke", "2행정");
    }
}
