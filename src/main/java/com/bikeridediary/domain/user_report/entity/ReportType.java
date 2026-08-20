package com.bikeridediary.domain.user_report.entity;

public enum ReportType {
    // 장소 삭제 제보 (target_place_id 세팅 필요)
    PLACE_DELETE,
    // 버그 리포트
    BUG_REPORT,
    // 기타 문의/개발자에게 할 말
    ETC
}