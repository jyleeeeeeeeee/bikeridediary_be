package com.bikeridediary.global.logging;

/**
 * 외부 API 식별자 String 상수.
 * enum이 아닌 이유: 새 API 추가 시 재컴파일 없이 어노테이션에 String literal로 삽입 가능.
 * api_call_logs.api_name 컬럼 저장 값이므로 한 번 정한 이름 변경 금지 (기존 로그와 불일치).
 * 어노테이션에 상수를 참조하지 않고 String literal("NAVER_DIRECTIONS")을 직접 써도 무방.
 */
public final class ApiNames {

    private ApiNames() {}

    public static final String NAVER_GEOCODING = "NAVER_GEOCODING";
    public static final String NAVER_REVERSE_GEOCODING = "NAVER_REVERSE_GEOCODING";
    public static final String NAVER_DIRECTIONS = "NAVER_DIRECTIONS";
    public static final String NAVER_SEARCH = "NAVER_SEARCH";
    public static final String KAKAO_LOCAL = "KAKAO_LOCAL";
    public static final String OPINET = "OPINET";
    public static final String OPENWEATHER = "OPENWEATHER";
}