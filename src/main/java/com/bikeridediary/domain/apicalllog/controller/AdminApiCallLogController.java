package com.bikeridediary.domain.apicalllog.controller;

import com.bikeridediary.domain.apicalllog.dto.ApiCallLogResponse;
import com.bikeridediary.domain.apicalllog.service.ApiCallLogService;
import com.bikeridediary.global.response.ApiResponse;
import com.bikeridediary.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "어드민 - 외부 API 호출 로그", description = "어드민만 접근 가능. 외부 API 사용량 모니터링.")
@RestController
@RequestMapping("/api/v1/admin/api-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiCallLogController {

    private final ApiCallLogService apiCallLogService;

    @Operation(summary = "외부 API 호출 로그 목록 (필터, 페이징)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ApiCallLogResponse>>> list(
            @Nullable @RequestParam(required = false) String apiName,
            @Nullable @RequestParam(required = false) UUID userId,
            @Nullable @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Nullable @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "calledAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                apiCallLogService.search(apiName, userId, from, to, pageable)
        ));
    }
}