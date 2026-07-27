package com.bikeridediary.domain.place.entity;

// 장소 변경 요청 종류
public enum PlaceChangeRequestType {
    CREATE,              // 신규 장소 등록
    UPDATE_COORDINATES,  // 좌표 수정
    UPDATE_INFO          // 이름/카테고리 수정
}