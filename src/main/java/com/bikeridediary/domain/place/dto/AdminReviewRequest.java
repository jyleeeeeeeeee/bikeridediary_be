package com.bikeridediary.domain.place.dto;

// 어드민 승인/거절 입력 (note는 승인엔 선택, 거절엔 사실상 필수 - 앱에서 강제)
public record AdminReviewRequest(
        String note
) {}