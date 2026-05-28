package com.bikeridediary.domain.maintenance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MaintenanceType {

    ENGINE_OIL("ENGINE_OIL", "엔진 오일"),
    CHAIN("CHAIN", "체인"),
    FRONT_TIRE("FRONT_TIRE", "앞 타이어"),
    FRONT_BRAKE_PAD("FRONT_BRAKE_PAD", "앞 브레이크 패드"),
    FRONT_BRAKE_FLUID("FRONT_BRAKE_FLUID", "앞 브레이크 액"),
    FRONT_SUSPENSION("FRONT_SUSPENSION", "앞 서스펜션 오일"),
    REAR_TIRE("REAR_TIRE", "뒷 타이어"),
    REAR_BRAKE_PAD("REAR_BRAKE_PAD", "뒷 브레이크 패드"),
    REAR_BRAKE_FLUID("REAR_BRAKE_FLUID", "뒷 브레이크 액"),
    REAR_SUSPENSION("REAR_SUSPENSION", "뒷 서스펜션 오일"),
    BATTERY("BATTERY", "배터리"),
    SPARK_PLUG("SPARK_PLUG", "점화플러그"),
    AIR_FILTER("AIR_FILTER", "에어필터"),
    COOLANT("COOLANT", "냉각수"),
    OTHER("OTHER", "기타");

    private final String code;
    private final String displayName;
}
