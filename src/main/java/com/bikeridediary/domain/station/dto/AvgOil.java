package com.bikeridediary.domain.station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AvgOil {
    @JsonProperty("TRADE_DT")
    private String tradeDt;
    @JsonProperty("PRODCD")
    private String prodcd;
    @JsonProperty("PRODNM")
    private String prodnm;
    @JsonProperty("PRICE")
    private double price;
    @JsonProperty("DIFF")
    private double diff;
}