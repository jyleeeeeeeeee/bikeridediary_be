package com.bikeridediary.domain.bikemodel.dto;

// 모델명 + 타입 응답 DTO (제조사별 모델 목록 조회용)
public record BikeModelNameResponse(String name, String type) {
}
