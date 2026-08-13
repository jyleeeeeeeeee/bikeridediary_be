package com.bikeridediary.global.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 외부 API 호출 로깅 어노테이션.
 * 이 어노테이션이 붙은 메서드는 ExternalApiLoggingAspect가 인터셉트하여
 * api_call_logs 테이블에 호출 이력을 기록한다.
 *
 * apiName은 String — ApiNames 상수 사용 권장 (오타 방지), 신규 API는 literal도 허용.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExternalApi {

    /** 호출하는 외부 API 식별자 (예: ApiNames.NAVER_DIRECTIONS 또는 "NAVER_DIRECTIONS") */
    String apiName();

    /**
     * 요청 파라미터 저장 여부.
     * true(기본)이면 파라미터를 Map으로 변환 후 민감 필드 제거하여 저장.
     */
    boolean logParams() default true;
}
