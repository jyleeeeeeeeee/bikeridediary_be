package com.bikeridediary.domain.bikemodel.dto;

import com.bikeridediary.domain.bikemodel.entity.BikeModelEntity;

public record BikeModelResponse(
        Long id,
        String name,
        Integer year,
        String type,
        String displacement,
        String engine,
        String power,
        String torque,
        String totalWeight,
        String seatHeight,
        String fuelCapacity
) {
    public static BikeModelResponse from(BikeModelEntity entity) {
        return new BikeModelResponse(
                entity.getId(),
                entity.getName(),
                entity.getYear(),
                entity.getType(),
                entity.getDisplacement(),
                entity.getEngine(),
                entity.getPower(),
                entity.getTorque(),
                entity.getTotalWeight(),
                entity.getSeatHeight(),
                entity.getFuelCapacity()
        );
    }
}
