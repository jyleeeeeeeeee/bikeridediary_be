package com.bikeridediary.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidScheduleIntervalValidator.class)
@Documented
public @interface ValidScheduleInterval {
    String message() default "km 기준 또는 개월 기준 정비 주기 중 하나 이상 입력해야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
