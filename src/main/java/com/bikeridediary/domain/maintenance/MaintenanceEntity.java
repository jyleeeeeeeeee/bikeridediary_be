package com.bikeridediary.domain.maintenance;

import com.bikeridediary.domain.bike.BikeEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenances")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceEntity {

    // 정비 기록 ID (UUID)
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

    // 정비 날짜
    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    // 정비 당시 주행거리 (km)
    @Column(name = "mileage_at_maintenance", nullable = false)
    private Integer mileageAtMaintenance;

    // 정비 비용 (원, nullable)
    @Column(name = "cost")
    private Integer cost;

    // 정비 메모 (500자 제한)
    @Column(name = "description", length = 500)
    private String description;

    // 다음 정비 예정 주행거리 (km, nullable)
    @Column(name = "next_due_km")
    private Integer nextDueKm;

    // 다음 정비 예정 날짜 (nullable)
    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    // 등록 일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 일시
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 삭제 일시 (소프트 삭제)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static MaintenanceEntity create(
            BikeEntity bikeEntity,
            MaintenanceType maintenanceType,
            LocalDate maintenanceDate,
            Integer mileageAtMaintenance,
            Integer cost,
            String description,
            Integer nextDueKm,
            LocalDate nextDueDate
    ) {
        MaintenanceEntity maintenanceEntity = new MaintenanceEntity();
        maintenanceEntity.bikeEntity = bikeEntity;
        maintenanceEntity.maintenanceType = maintenanceType;
        maintenanceEntity.maintenanceDate = maintenanceDate;
        maintenanceEntity.mileageAtMaintenance = mileageAtMaintenance;
        maintenanceEntity.cost = cost;
        maintenanceEntity.description = description;
        maintenanceEntity.nextDueKm = nextDueKm;
        maintenanceEntity.nextDueDate = nextDueDate;
        return maintenanceEntity;
    }

    public void update(
            MaintenanceType maintenanceType,
            LocalDate maintenanceDate,
            Integer mileageAtMaintenance,
            Integer cost,
            String description,
            Integer nextDueKm,
            LocalDate nextDueDate
    ) {
        this.maintenanceType = maintenanceType;
        this.maintenanceDate = maintenanceDate;
        this.mileageAtMaintenance = mileageAtMaintenance;
        this.cost = cost;
        this.description = description;
        this.nextDueKm = nextDueKm;
        this.nextDueDate = nextDueDate;
    }

    public void setNextMaintenanceDue(Integer nextDueKm, LocalDate nextDueDate) {
        this.nextDueKm = nextDueKm;
        this.nextDueDate = nextDueDate;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isOwner(UUID userId) {
        return this.bikeEntity.isOwner(userId);
    }
}
