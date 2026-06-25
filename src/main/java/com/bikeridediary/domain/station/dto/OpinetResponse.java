package com.bikeridediary.domain.station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OpinetResponse<T> {

    @JsonProperty("RESULT")
    private Result<T> result;

    @Getter
    @NoArgsConstructor
    public static class Result<T> {
        @JsonProperty("OIL")
        private List<T> oils;
    }
}