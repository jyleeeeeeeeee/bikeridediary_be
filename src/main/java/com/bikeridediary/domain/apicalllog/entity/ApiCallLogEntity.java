package com.bikeridediary.domain.apicalllog.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import org.hibernate.annotations.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "api_call_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiCallLogEntity {
    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval)
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    // 로그 고유 ID (UUID)
    @Id
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    // 호출한 유저 ID (미인증 호출 시 null. FK → users ON DELETE SET NULL)
    @Column(name = "user_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID userId;

    // 외부 API 식별자
    @Column(name = "api_name", nullable = false, length = 50)
    private String apiName;

    // 실제 호출 URL path (쿼리스트링 제외. URL 파악 안 되면 클래스명.메서드명)
    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;

    // HTTP 메서드
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    // 응답 HTTP status (예외 발생 시 null)
    @Column(name = "status_code")
    private Integer statusCode;

    // 응답 소요 시간 (밀리초)
    @Column(name = "response_time_ms", nullable = false)
    private Integer responseTimeMs;

    // 마스킹된 요청 파라미터 (JSONB. API Key/Secret 제거됨)
    @Type(JsonType.class)
    @Column(name = "request_params", columnDefinition = "jsonb")
    private Map<String, Object> requestParams;

    // 예외 발생 시 에러 메시지
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // 호출 시각
    @Column(name = "called_at", nullable = false)
    private LocalDateTime calledAt;

    public static ApiCallLogEntity create(
            UUID userId,
            String apiName,
            String endpoint,
            String httpMethod,
            Integer statusCode,
            int responseTimeMs,
            Map<String, Object> requestParams,
            String errorMessage
    ) {
        ApiCallLogEntity e = new ApiCallLogEntity();
        e.id = UUID.randomUUID();
        e.userId = userId;
        e.apiName = apiName;
        e.endpoint = truncate(endpoint, 200);
        e.httpMethod = httpMethod;
        e.statusCode = statusCode;
        e.responseTimeMs = responseTimeMs;
        e.requestParams = requestParams;
        e.errorMessage = errorMessage;
        e.calledAt = LocalDateTime.now();
        return e;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) return "unknown";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
