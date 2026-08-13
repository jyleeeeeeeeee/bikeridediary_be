package com.bikeridediary.domain.apicalllog.dto;

import com.bikeridediary.domain.apicalllog.entity.ApiCallLogEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ApiCallLogResponse(
        Long no,
        UUID id,
        UUID userId,
        String apiName,
        String endpoint,
        String httpMethod,
        Integer statusCode,
        Integer responseTimeMs,
        Map<String, Object> requestParams,
        String errorMessage,
        LocalDateTime calledAt
) {
    public static ApiCallLogResponse from(ApiCallLogEntity entity) {
        return new ApiCallLogResponse(
                entity.getNo(), entity.getId(), entity.getUserId(),
                entity.getApiName(), entity.getEndpoint(), entity.getHttpMethod(),
                entity.getStatusCode(), entity.getResponseTimeMs(),
                entity.getRequestParams(), entity.getErrorMessage(),
                entity.getCalledAt()
        );
    }
}