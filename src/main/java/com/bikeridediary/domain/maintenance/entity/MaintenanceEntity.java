package com.bikeridediary.domain.maintenance.entity;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// 정비 기록 엔티티 - 실제 수행된 정비 내역을 기록하고 관리
@Entity
@Table(name = "maintenances")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceEntity extends BaseEntity {

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
    private Long mileageAtMaintenance;

    // 정비 비용 (원, nullable)
    @Column(name = "cost")
    private Long cost;

    // 정비 메모 (500자 제한)
    @Column(name = "description", length = 500)
    private String description;

    // 다음 정비 예정 주행거리 (km, nullable)
    @Column(name = "next_due_km")
    private Long nextDueKm;

    // 다음 정비 예정 날짜 (nullable)
    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    // 정비 기록 엔티티 생성
    public static MaintenanceEntity create(
            BikeEntity bikeEntity,
            MaintenanceType maintenanceType,
            LocalDate maintenanceDate,
            Long mileageAtMaintenance,
            Long cost,
            String description,
            Long nextDueKm,
            LocalDate nextDueDate,
            String imageUrls
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
        maintenanceEntity.imageUrls = imageUrls;
        return maintenanceEntity;
    }

    // 정비 기록 정보 수정
    public void update(
            MaintenanceType maintenanceType,
            LocalDate maintenanceDate,
            Long mileageAtMaintenance,
            Long cost,
            String description,
            Long nextDueKm,
            LocalDate nextDueDate,
            String imageUrls
    ) {
        this.maintenanceType = maintenanceType;
        this.maintenanceDate = maintenanceDate;
        this.mileageAtMaintenance = mileageAtMaintenance;
        this.cost = cost;
        this.description = description;
        this.nextDueKm = nextDueKm;
        this.nextDueDate = nextDueDate;
        this.imageUrls = imageUrls;
    }

    // 다음 정비 예정 정보 설정 (km, 날짜)
    public void setNextMaintenanceDue(Long nextDueKm, LocalDate nextDueDate) {
        this.nextDueKm = nextDueKm;
        this.nextDueDate = nextDueDate;
    }

    // 이 정비 기록이 특정 사용자의 바이크에 속하는지 권한 검증
    public boolean isOwner(UUID userId) {
        return this.bikeEntity.isOwner(userId);
    }

}
