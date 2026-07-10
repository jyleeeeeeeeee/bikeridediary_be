package com.bikeridediary.domain.bike.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BikeSyncRequest(
        @NotNull UUID id,
        @NotBlank String manufacturerName,
        @NotBlank String modelName,
        @NotNull Integer year,
        String category,
        @NotNull @PositiveOrZero Long totalMileageKm,
        boolean isRepresentative,
        LocalDate purchasedAt,
        String photoUrl,
        String memo,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime updatedAt,
        LocalDateTime deletedAt) {
}
