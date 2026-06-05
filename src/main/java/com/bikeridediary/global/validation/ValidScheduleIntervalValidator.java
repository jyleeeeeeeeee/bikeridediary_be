package com.bikeridediary.global.validation;

import com.bikeridediary.domain.maintenance.dto.ScheduleIntervalCheckable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidScheduleIntervalValidator implements ConstraintValidator<ValidScheduleInterval, ScheduleIntervalCheckable> {

    @Override
    public boolean isValid(ScheduleIntervalCheckable value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.intervalKm() != null || value.intervalMonths() != null;
    }
}
