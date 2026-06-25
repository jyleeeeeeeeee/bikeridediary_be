package com.bikeridediary.domain.station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StationOil {
    @JsonProperty("UNI_ID")
    private String uniId;
    @JsonProperty("POLL_DIV_CD")
    private String pollDivCd;
    @JsonProperty("OS_NM")
    private String osNm;
    @JsonProperty("PRICE")
    private float price;
    @JsonProperty("DISTANCE")
    private float distance;
    @JsonProperty("GIS_X_COOR")
    private double gisXCoor;
    @JsonProperty("GIS_Y_COOR")
    private double gisYCoor;
}
