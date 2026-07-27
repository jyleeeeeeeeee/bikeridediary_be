package com.bikeridediary.domain.place.dto;

public record UpdateInfoPayload(
        String placeName,
        String category,
        String description   // 선택 필드. null이면 기존 값 유지, 빈 문자열이면 삭제 (서비스에서 결정)
) {}
