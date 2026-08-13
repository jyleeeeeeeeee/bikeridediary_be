package com.bikeridediary.domain.apicalllog.service;

import com.bikeridediary.domain.apicalllog.repository.ApiCallLogRepository;
import com.bikeridediary.domain.apicalllog.dto.ApiCallLogResponse;
import com.bikeridediary.domain.apicalllog.entity.ApiCallLogEntity;
import com.bikeridediary.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCallLogService {

    private final ApiCallLogRepository apiCallLogRepository;

    /**
     * 외부 API 호출 로그 저장.
     * REQUIRES_NEW: 호출자의 트랜잭션과 완전히 분리.
     * 로그 저장 실패 시 원 API 호출 결과에 영향 없도록 예외 catch → warn log만.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(
            UUID userId,
            String apiName,
            String endpoint,
            String httpMethod,
            Integer statusCode,
            int responseTimeMs,
            Map<String, Object> requestParams,
            String errorMessage
    ) {
        try {
            ApiCallLogEntity entity = ApiCallLogEntity.create(
                    userId, apiName, endpoint, httpMethod,
                    statusCode, responseTimeMs, requestParams, errorMessage
            );
            apiCallLogRepository.save(entity);
        } catch (Exception e) {
            log.warn("[ApiCallLog] 로그 저장 실패 (무시): apiName={}, endpoint={}, error={}",
                    apiName, endpoint, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ApiCallLogResponse> search(
            String apiName,
            UUID userId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        return PageResponse.of(
                apiCallLogRepository.search(apiName, userId, from, to, pageable),
                ApiCallLogResponse::from
        );
    }
}