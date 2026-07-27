package com.bikeridediary.domain.place.dto;

import com.bikeridediary.domain.place.repository.PlaceRegistrationCount;

import java.util.UUID;

// 유저별 장소 등록 순위 응답
public record PlaceRankingResponse(
        int rank,
        UUID userId,
        String nickname,
        long count
) {
    public static PlaceRankingResponse of(int rank, PlaceRegistrationCount c) {
        return new PlaceRankingResponse(rank, c.getUserId(), c.getNickname(), c.getCount());
    }
}
