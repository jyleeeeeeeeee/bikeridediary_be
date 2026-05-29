package com.bikeridediary.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 비즈니스 로직 오류를 위한 기본 예외 클래스. ErrorCode enum으로 오류 케이스를 정의한다.
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
