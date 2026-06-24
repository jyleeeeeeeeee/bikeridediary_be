package com.bikeridediary.domain.station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OilResponse(
    @JsonProperty("RESULT") Result result
) {
    public record Result(
        @JsonProperty("OIL") List<Oil> oil
    ) {}

    public static record Oil(
            @JsonProperty("TRADE_DT") String tradeDt,
            @JsonProperty("PRODCD") String prodcd,
            @JsonProperty("PRODNM") String prodnm,
            @JsonProperty("PRICE") float price,
            @JsonProperty("DIFF") float diff
    ) {}
}