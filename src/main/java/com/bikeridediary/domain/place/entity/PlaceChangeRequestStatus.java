package com.bikeridediary.domain.place.entity;

// 장소 변경 요청 상태
public enum PlaceChangeRequestStatus {
    PENDING,   // 승인 대기
    APPROVED,  // 승인 완료 (places 반영됨)
    REJECTED   // 거절
}