package com.bikeridediary.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 애플리케이션 전체 에러 코드 정의
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 입력값입니다"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "리소스를 찾을 수 없습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근이 거부되었습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"),

    // 인증
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다"),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_EXPIRED_TOKEN", "만료된 토큰입니다"),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다"),
    AUTH_APPLE_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_APPLE_FAILED", "Apple 로그인 인증에 실패했습니다"),
    AUTH_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_UNSUPPORTED_PROVIDER", "지원하지 않는 OAuth2 제공자입니다"),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 잘못되었습니다"),
    AUTH_USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH_USER_ALREADY_EXISTS", "이미 가입된 이메일입니다"),
    AUTH_EMAIL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "AUTH_EMAIL_INVALID_FORMAT", "이메일 형식이 올바르지 않습니다"),
    AUTH_PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_TOO_WEAK", "비밀번호 강도가 미달됩니다 (8자 이상, 특수문자 포함)"),
    AUTH_NAVER_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_NAVER_VERIFICATION_FAILED", "네이버 인증에 실패했습니다"),


    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),

    // 바이크
    BIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "BIKE_NOT_FOUND", "바이크를 찾을 수 없습니다"),
    BIKE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BIKE_ACCESS_DENIED", "해당 바이크에 대한 권한이 없습니다"),
    BIKE_MILEAGE_INVALID(HttpStatus.BAD_REQUEST, "BIKE_MILEAGE_INVALID", "주행거리는 0 이상이어야 합니다"),

    // 정비
    MAINTENANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "MAINTENANCE_NOT_FOUND", "정비 기록을 찾을 수 없습니다"),
    MAINTENANCE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MAINTENANCE_ACCESS_DENIED", "해당 정비 기록에 대한 권한이 없습니다"),

    // 정비 주기
    MAINTENANCE_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "MAINTENANCE_SCHEDULE_NOT_FOUND", "정비 주기를 찾을 수 없습니다"),
    MAINTENANCE_SCHEDULE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MAINTENANCE_SCHEDULE_ACCESS_DENIED", "해당 정비 주기에 대한 권한이 없습니다"),
    MAINTENANCE_SCHEDULE_DUPLICATE(HttpStatus.BAD_REQUEST, "MAINTENANCE_SCHEDULE_DUPLICATE", "해당 바이크에 동일한 정비 종류의 주기가 이미 존재합니다"),

    // 코스
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "코스를 찾을 수 없습니다"),
    COURSE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COURSE_ACCESS_DENIED", "해당 코스에 대한 권한이 없습니다"),
    COURSE_GPX_PARSE_FAILED(HttpStatus.BAD_REQUEST, "COURSE_GPX_PARSE_FAILED", "GPX 파일 파싱에 실패했습니다"),
    COURSE_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "COURSE_INVALID_FILE", "GPX 파일만 업로드 가능합니다"),

    // 주유
    FUELING_NOT_FOUND(HttpStatus.NOT_FOUND, "FUELING_NOT_FOUND", "주유 기록을 찾을 수 없습니다"),
    FUELING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FUELING_ACCESS_DENIED", "해당 주유 기록에 대한 권한이 없습니다"),

    // 파일 업로드
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_SIZE_EXCEEDED", "파일 크기가 제한을 초과했습니다 (50MB)"),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FILE_ACCESS_DENIED", "파일에 접근할 권한이 없습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),

    // 외부 API
    NAVER_API_ERROR(HttpStatus.BAD_GATEWAY, "NAVER_API_ERROR", "네이버 API 호출에 실패했습니다"),
    WEATHER_API_ERROR(HttpStatus.BAD_GATEWAY, "WEATHER_API_ERROR", "날씨 API 호출에 실패했습니다"),

    // 지도 DB 조회
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", "장소 조회에 실패했습니다."),
    PLACE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_CATEGORY_NOT_FOUND", "카테고리 조회에 실패했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
