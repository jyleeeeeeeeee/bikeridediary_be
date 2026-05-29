package com.bikeridediary.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 공통 API 응답 래퍼. 모든 API 응답은 일관성을 위해 이 형식을 사용한다.
// 성공: { "success": true, "data": { ... } }
// 오류: { "success": false, "error": { "code": "...", "message": "..." } }
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail error;

    // 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    // 성공 응답 (데이터 없음, e.g. DELETE)
    public static <T> ApiResponse<T> ok() {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        return response;
    }

    // 오류 응답
    public static <T> ApiResponse<T> fail(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorDetail(code, message);
        return response;
    }

    @Getter
    public static class ErrorDetail {
        private final String code;
        private final String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
