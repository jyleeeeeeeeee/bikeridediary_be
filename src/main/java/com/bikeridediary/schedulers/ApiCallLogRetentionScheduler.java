package com.bikeridediary.schedulers;

import com.bikeridediary.domain.apicalllog.repository.ApiCallLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallLogRetentionScheduler {

    private static final int RETENTION_DAYS = 90;

    private final ApiCallLogRepository apiCallLogRepository;

    /**
     * 매일 새벽 3시 실행.
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredLogs() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deletedCount = apiCallLogRepository.deleteByCalledAtBefore(threshold);
        log.info("[ApiCallLogRetention] {}일 경과 로그 {}건 삭제 (기준: {})",
                RETENTION_DAYS, deletedCount, threshold);
    }
}