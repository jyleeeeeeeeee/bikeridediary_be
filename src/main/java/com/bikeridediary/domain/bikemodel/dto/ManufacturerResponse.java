package com.bikeridediary.domain.bikemodel.dto;

import com.bikeridediary.domain.bikemodel.entity.ManufacturerEntity;

public record ManufacturerResponse(
        Long id,
        String apiName,
        String displayNameKo,
        String country,
        int displayOrder,
        String logoUrl
) {
    public static ManufacturerResponse from(ManufacturerEntity entity) {
        String logoFilename = entity.getApiName().toLowerCase().replace(" ", "_") + ".png";
        return new ManufacturerResponse(
                entity.getId(),
                entity.getApiName(),
                entity.getDisplayNameKo(),
                entity.getCountry(),
                entity.getDisplayOrder(),
                "/logos/" + logoFilename
        );
    }
}
