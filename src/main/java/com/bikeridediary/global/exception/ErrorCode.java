package com.bikeridediary.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * All error codes used in the application.
 * Format: DOMAIN_ERROR_DESCRIPTION
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Invalid input value"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error"),

    // 인증
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN", "Invalid token"),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_EXPIRED_TOKEN", "Token has expired"),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH_TOKEN", "Invalid refresh token"),
    AUTH_APPLE_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_APPLE_FAILED", "Apple sign-in verification failed"),
    AUTH_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_UNSUPPORTED_PROVIDER", "Unsupported OAuth2 provider"),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 잘못됨"),
    AUTH_USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH_USER_ALREADY_EXISTS", "이미 가입된 이메일"),
    AUTH_EMAIL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "AUTH_EMAIL_INVALID_FORMAT", "이메일 형식이 올바르지 않음"),
    AUTH_PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_TOO_WEAK", "비밀번호 강도 미달(8자 미만, 특수문자 없음 등)"),
    AUTH_NAVER_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_NAVER_VERIFICATION_FAILED", "네이버 인증 실패"),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"),

    // 바이크
    BIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "BIKE_NOT_FOUND", "Bike not found"),
    BIKE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BIKE_ACCESS_DENIED", "Not your bike"),
    BIKE_MILEAGE_INVALID(HttpStatus.BAD_REQUEST, "BIKE_MILEAGE_INVALID", "Mileage must be 0 or greater"),

    // 정비
    MAINTENANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "MAINTENANCE_NOT_FOUND", "Maintenance record not found"),
    MAINTENANCE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MAINTENANCE_ACCESS_DENIED", "Access denied"),

    // 코스
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Course not found"),
    COURSE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COURSE_ACCESS_DENIED", "Access denied"),
    COURSE_GPX_PARSE_FAILED(HttpStatus.BAD_REQUEST, "COURSE_GPX_PARSE_FAILED", "Failed to parse GPX file"),
    COURSE_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "COURSE_INVALID_FILE", "Only GPX files are allowed"),

    // 주유
    FUELING_NOT_FOUND(HttpStatus.NOT_FOUND, "FUELING_NOT_FOUND", "Fueling record not found"),

    // 파일 업로드
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "File upload failed"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_SIZE_EXCEEDED", "File size exceeds limit (50MB)"),

    // 외부 API
    NAVER_API_ERROR(HttpStatus.BAD_GATEWAY, "NAVER_API_ERROR", "Naver API call failed"),
    WEATHER_API_ERROR(HttpStatus.BAD_GATEWAY, "WEATHER_API_ERROR", "Weather API call failed");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
