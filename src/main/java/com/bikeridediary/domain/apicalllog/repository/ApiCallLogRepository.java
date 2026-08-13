package com.bikeridediary.domain.apicalllog.repository;

import com.bikeridediary.domain.apicalllog.entity.ApiCallLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ApiCallLogRepository extends JpaRepository<ApiCallLogEntity, UUID> {

    @Query("""
            SELECT a FROM ApiCallLogEntity a
            WHERE (:apiName IS NULL OR a.apiName = :apiName)
              AND (:userId IS NULL OR a.userId = :userId)
              AND (:from IS NULL OR a.calledAt >= :from)
              AND (:to IS NULL OR a.calledAt <= :to)
            ORDER BY a.calledAt DESC
            """)
    Page<ApiCallLogEntity> search(
            @Param("apiName") String apiName,
            @Param("userId") UUID userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ApiCallLogEntity a WHERE a.calledAt < :threshold")
    int deleteByCalledAtBefore(@Param("threshold") LocalDateTime threshold);
}