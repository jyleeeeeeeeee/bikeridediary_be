package com.bikeridediary.domain.user_report.entity;

public enum ReportStatus {
    // 접수 (기본)
    REPORTED,
    // 관리자 처리 중
    PROCEEDING,
    // 처리 완료
    DONE,
    // 반려
    REJECT
}