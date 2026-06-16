package com.bikeridediary.domain.fueling.entity;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// 주유 기록 엔티티 - 주유량, 금액, 연비 계산 관리
@Entity
@Table(name = "fuelings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FuelingEntity extends BaseEntity {

    // 주유 기록 ID (UUID)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 소유 바이크 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bike_id", nullable = false)
    @JsonBackReference
    private BikeEntity bikeEntity;

    // 주유 날짜
    @Column(name = "fueling_date", nullable = false)
    private LocalDate fuelingDate;

    // 주유 시 주행거리 (km)
    @Column(name = "mileage_at_fueling", nullable = false)
    private Integer mileageAtFueling;

    // 주유량 (리터, 소수점 2자리)
    @Column(name = "fuel_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal fuelAmount;

    // 리터당 가격 (원)
    @Column(name = "price_per_liter")
    private Integer pricePerLiter;

    // 총 주유 비용 (원)
    @Column(name = "total_cost")
    private Integer totalCost;

    // 연료 종류 (일반유/고급유/경유)
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 10)
    private FuelType fuelType;

    // 만탱크 여부 (연비 계산의 핵심 조건)
    @Column(name = "is_full_tank", nullable = false)
    private boolean isFullTank;

    // 계산된 연비 (km/L, 만탱크법 기반 — 만탱크 시에만 계산 가능)
    @Column(name = "fuel_efficiency", precision = 6, scale = 2)
    private BigDecimal fuelEfficiency;

    // 메모
    @Column(length = 500)
    private String memo;

    // 주유소명
    @Column(name = "station_name", length = 100)
    private String stationName;

    // 주유 기록 생성
    public static FuelingEntity create(
            BikeEntity bikeEntity,
            LocalDate fuelingDate,
            Integer mileageAtFueling,
            BigDecimal fuelAmount,
            Integer pricePerLiter,
            Integer totalCost,
            FuelType fuelType,
            boolean isFullTank,
            String memo,
            String stationName
    ) {
        FuelingEntity entity = new FuelingEntity();
        entity.bikeEntity = bikeEntity;
        entity.fuelingDate = fuelingDate;
        entity.mileageAtFueling = mileageAtFueling;
        entity.fuelAmount = fuelAmount;
        entity.pricePerLiter = pricePerLiter;
        entity.totalCost = totalCost;
        entity.fuelType = fuelType;
        entity.isFullTank = isFullTank;
        entity.memo = memo;
        entity.stationName = stationName;
        return entity;
    }

    // 주유 기록 수정
    public void update(
            LocalDate fuelingDate,
            Integer mileageAtFueling,
            BigDecimal fuelAmount,
            Integer pricePerLiter,
            Integer totalCost,
            FuelType fuelType,
            boolean isFullTank,
            String memo,
            String stationName
    ) {
        this.fuelingDate = fuelingDate;
        this.mileageAtFueling = mileageAtFueling;
        this.fuelAmount = fuelAmount;
        this.pricePerLiter = pricePerLiter;
        this.totalCost = totalCost;
        this.fuelType = fuelType;
        this.isFullTank = isFullTank;
        this.memo = memo;
        this.stationName = stationName;
    }

    // 연비 설정 (서비스에서 만탱크법으로 계산 후 주입)
    public void setFuelEfficiency(BigDecimal fuelEfficiency) {
        this.fuelEfficiency = fuelEfficiency;
    }

    // 이 주유 기록이 특정 사용자의 바이크에 속하는지 권한 검증
    public boolean isOwner(UUID userId) {
        return this.bikeEntity.isOwner(userId);
    }
}
