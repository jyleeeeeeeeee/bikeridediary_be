package com.bikeridediary.domain.bikemodel.dto;

import com.bikeridediary.domain.bikemodel.entity.ManufacturerEntity;

public record ManufacturerResponse(
        String manufacturerName,
        String displayNameKo,
        String country,
        int displayOrder,
        String imageUrl
) {
    public static ManufacturerResponse from(ManufacturerEntity entity) {
        String imageUrl = entity.getImageFile() != null
                ? "/logos/" + entity.getImageFile()
                : null;
        return new ManufacturerResponse(
                entity.getManufacturerName(),
                entity.getDisplayNameKo(),
                entity.getCountry(),
                entity.getDisplayOrder(),
                imageUrl
        );
    }
}
