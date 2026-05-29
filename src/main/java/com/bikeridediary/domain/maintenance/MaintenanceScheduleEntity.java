package com.bikeridediary.domain.maintenance;

import com.bikeridediary.domain.bike.BikeEntity;
import com.bikeridediary.domain.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "maintenance_schedules")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceScheduleEntity extends BaseEntity {

    // 정비 주기 ID (UUID)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 소유 바이크 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bike_id", nullable = false)
    @JsonBackReference
    private BikeEntity bikeEntity;

    // 정비 종류 (엔진오일, 체인, 타이어, 브레이크 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceType maintenanceType;

    // km 기준 정비 주기 (nullable - 없으면 km 기준 정비 안 함)
    @Column(name = "interval_km")
    private Integer intervalKm;

    // 개월 기준 정비 주기 (nullable - 없으면 날짜 기준 정비 안 함)
    @Column(name = "interval_months")
    private Integer intervalMonths;

    // 마지막 정비 시 주행거리 (km, nullable)
    @Column(name = "last_maintenance_mileage")
    private Integer lastMaintenanceMileage;

    // 마지막 정비 날짜 (nullable)
    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    public static MaintenanceScheduleEntity create(
            BikeEntity bikeEntity,
            MaintenanceType maintenanceType,
            Integer intervalKm,
            Integer intervalMonths,
            Integer lastMaintenanceMileage,
            LocalDate lastMaintenanceDate
    ) {
        MaintenanceScheduleEntity maintenanceScheduleEntity = new MaintenanceScheduleEntity();
        maintenanceScheduleEntity.bikeEntity = bikeEntity;
        maintenanceScheduleEntity.maintenanceType = maintenanceType;
        maintenanceScheduleEntity.intervalKm = intervalKm;
        maintenanceScheduleEntity.intervalMonths = intervalMonths;
        maintenanceScheduleEntity.lastMaintenanceMileage = lastMaintenanceMileage;
        maintenanceScheduleEntity.lastMaintenanceDate = lastMaintenanceDate;
        return maintenanceScheduleEntity;
    }

    public void update(
            MaintenanceType maintenanceType,
            Integer intervalKm,
            Integer intervalMonths,
            Integer lastMaintenanceMileage,
            LocalDate lastMaintenanceDate) {
        this.maintenanceType = maintenanceType;
        this.intervalKm = intervalKm;
        this.intervalMonths = intervalMonths;
        this.lastMaintenanceMileage = lastMaintenanceMileage;
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public void updateLastMaintenanceDate(
            Integer lastMaintenanceMileage,
            LocalDate lastMaintenanceDate) {
        this.lastMaintenanceMileage = lastMaintenanceMileage;
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public boolean isOverdueByKm(Integer currentMileage) {
        if (intervalKm == null || lastMaintenanceMileage == null) {
            return false;
        }
        return currentMileage >= (lastMaintenanceMileage + intervalKm);
    }

    public boolean isOverdueByDate(LocalDate currentDate) {
        if (intervalMonths == null || lastMaintenanceDate == null) {
            return false;
        }
        return currentDate.isAfter(lastMaintenanceDate.plusMonths(intervalMonths));
    }

    public boolean isOverdue(Integer currentMileage, LocalDate currentDate) {
        return isOverdueByKm(currentMileage) || isOverdueByDate(currentDate);
    }

    public boolean isOwner(UUID userId) {
        return this.bikeEntity.isOwner(userId);
    }

}
