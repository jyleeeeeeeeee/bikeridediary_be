package com.bikeridediary.domain.fueling.entity;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fuelings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FuelingEntity extends BaseEntity {

    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval)
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bike_id", nullable = false)
    @JsonBackReference
    private BikeEntity bikeEntity;

    @Column(name = "fueling_date", nullable = false)
    private LocalDate fuelingDate;

    @Column(name = "mileage_at_fueling", nullable = false)
    private Long mileageAtFueling;

    @Column(name = "fuel_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal fuelAmount;

    @Column(name = "price_per_liter")
    private Long pricePerLiter;

    @Column(name = "total_cost")
    private Long totalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 10)
    private FuelType fuelType;

    @Column(name = "fuel_efficiency", precision = 6, scale = 2)
    private BigDecimal fuelEfficiency;

    @Column(length = 500)
    private String memo;

    @Column(name = "station_name", length = 100)
    private String stationName;

    public static FuelingEntity create(
            BikeEntity bikeEntity,
            LocalDate fuelingDate,
            Long mileageAtFueling,
            BigDecimal fuelAmount,
            Long pricePerLiter,
            Long totalCost,
            FuelType fuelType,
            String memo,
            String stationName
    ) {
        return createWithId(UUID.randomUUID(), bikeEntity, fuelingDate, mileageAtFueling,
                fuelAmount, pricePerLiter,totalCost, fuelType, memo, stationName);
    }

    public static FuelingEntity createWithId(
            UUID id,
            BikeEntity bikeEntity,
            LocalDate fuelingDate,
            Long mileageAtFueling,
            BigDecimal fuelAmount,
            Long pricePerLiter,
            Long totalCost,
            FuelType fuelType,
            String memo,
            String stationName
    ) {
        FuelingEntity entity = new FuelingEntity();
        entity.id = id;
        entity.bikeEntity = bikeEntity;
        entity.fuelingDate = fuelingDate;
        entity.mileageAtFueling = mileageAtFueling;
        entity.fuelAmount = fuelAmount;
        entity.pricePerLiter = pricePerLiter;
        entity.totalCost = totalCost;
        entity.fuelType = fuelType;
        entity.memo = memo;
        entity.stationName = stationName;
        return entity;
    }

    public void update(
            LocalDate fuelingDate,
            Long mileageAtFueling,
            BigDecimal fuelAmount,
            Long pricePerLiter,
            Long totalCost,
            FuelType fuelType,
            String memo,
            String stationName
    ) {
        this.fuelingDate = fuelingDate;
        this.mileageAtFueling = mileageAtFueling;
        this.fuelAmount = fuelAmount;
        this.pricePerLiter = pricePerLiter;
        this.totalCost = totalCost;
        this.fuelType = fuelType;
        this.memo = memo;
        this.stationName = stationName;
    }

    public void setFuelEfficiency(BigDecimal fuelEfficiency) {
        this.fuelEfficiency = fuelEfficiency;
    }

    public boolean isOwner(UUID userId) {
        return this.bikeEntity.isOwner(userId);
    }
}
