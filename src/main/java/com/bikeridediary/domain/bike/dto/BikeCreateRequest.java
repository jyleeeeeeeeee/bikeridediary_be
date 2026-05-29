package com.bikeridediary.domain.bike.dto;

import com.bikeridediary.domain.bike.entity.BikeCategory;
import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a new bike.
 */
public record BikeCreateRequest(
        @NotBlank(message = "Manufacturer name is required")
        @Size(max = 100, message = "Manufacturer name must not exceed 100 characters")
        String manufacturerName,

        @NotBlank(message = "Model name is required")
        @Size(max = 100, message = "Model name must not exceed 100 characters")
        String modelName,

        @NotNull(message = "Year is required")
        @Min(value = 1900, message = "Year must be 1900 or later")
        @Max(value = 2100, message = "Year must not exceed 2100")
        Integer year,

        @NotNull(message = "Category is required")
        BikeCategory category,

        @NotNull(message = "Total mileage is required")
        @Min(value = 0, message = "Total mileage must be 0 or greater")
        Integer totalMileageKm
) {
}
