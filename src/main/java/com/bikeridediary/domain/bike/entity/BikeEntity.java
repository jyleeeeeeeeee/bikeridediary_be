package com.bikeridediary.domain.bike.entity;

import com.bikeridediary.domain.common.entity.BaseEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceEntity;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 바이크 엔티티 - 사용자가 소유한 바이크 정보 및 정비 이력을 관리 (MVP: 제조사/모델명은 텍스트 직접 입력, 2차: 모델 DB 연동 예정)
@Entity
@Table(name = "bikes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BikeEntity extends BaseEntity {

    // 바이크 ID (UUID)
    @Id
    @Column(name = "id")
    private UUID id;

    // 소유 사용자 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private UserEntity userEntity;

    // 제조사명 (MVP: 텍스트 직접 입력, 2차: bike_trims FK 연동 예정)
    @Column(name = "manufacturer_name", nullable = false, length = 100)
    private String manufacturerName;

    // 모델명 (MVP: 텍스트 직접 입력, 2차: bike_trims FK 연동 예정)
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    // 연식
    @Column(nullable = false)
    private Integer year;

    // 바이크 카테고리 (bike_models.type 값 사용: Sport, Naked bike, Touring 등)
    @Column(length = 50)
    private String category;

    // 현재 총 주행거리 (km) - 사용자가 수동으로 업데이트
    @Setter
    @Column(name = "total_mileage_km", nullable = false)
    private Long totalMileageKm;

    // 대표 바이크 여부 (정비/라이딩 기록의 기본값으로 사용)
    @Column(name = "is_representative", nullable = false)
    private boolean isRepresentative = false;

    // 구매 일자
    @Column(name = "purchased_at")
    private LocalDate purchasedAt;

    // 바이크 사진 URL (S3)
    @Column(name = "photo_url")
    private String photoUrl;

    // 메모
    @Column(length = 500)
    private String memo;

    // 최근 연비 (km/L)
    @Column(name = "latest_fuel_efficiency", precision = 6, scale = 2)
    private BigDecimal latestFuelEfficiency;

    // 평균 연비 (km/L)
    @Column(name = "average_fuel_efficiency", precision = 6, scale = 2)
    private BigDecimal averageFuelEfficiency;

    @Column(name = "is_exist_model", nullable = false)
    private boolean isExistModel = true;

    // 바이크의 정비 이력 목록 (양방향 One-To-Many)
    @OneToMany(mappedBy = "bikeEntity", cascade = CascadeType.PERSIST, orphanRemoval = false)
    @JsonManagedReference
    private List<MaintenanceEntity> maintenances = new ArrayList<>();

    // 바이크 엔티티 생성
    public static BikeEntity create(
            UserEntity userEntity,
            String manufacturerName,
            String modelName,
            Integer year,
            String category,
            Long totalMileageKm,
            boolean isExistModel
    ) {
        return createBikeEntity(null, userEntity, manufacturerName, modelName, year, category, totalMileageKm, isExistModel);
    }

    public static BikeEntity createWithId(
            UUID id,
            UserEntity userEntity,
            String manufacturerName,
            String modelName,
            Integer year,
            String category,
            Long totalMileageKm,
            boolean isExistModel
    ) {
        return createBikeEntity(id, userEntity, manufacturerName, modelName, year, category, totalMileageKm, isExistModel);
    }

    // 바이크 정보 수정
    public void update(
            String manufacturerName,
            String modelName,
            Integer year,
            String category,
            Long totalMileageKm,
            LocalDate purchasedAt,
            String memo,
            boolean isExistModel
    ) {
        this.manufacturerName = manufacturerName;
        this.modelName = modelName;
        this.year = year;
        this.category = category;
        this.totalMileageKm = totalMileageKm;
        this.purchasedAt = purchasedAt;
        this.memo = memo;
        this.isExistModel = isExistModel;
    }

    // 대표 바이크 여부 설정 (사용자의 기본 바이크로 지정)
    public void setRepresentative(boolean isRepresentative) {
        this.isRepresentative = isRepresentative;
    }

    // 바이크 사진 URL 업데이트
    public void updatePhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // 총 주행거리 갱신
    public void updateMileage(Long mileageKm) {
        this.totalMileageKm = mileageKm;
    }

    // 연비 갱신
    public void updateFuelEfficiency(BigDecimal latestFuelEfficiency, BigDecimal averageFuelEfficiency) {
        this.latestFuelEfficiency = latestFuelEfficiency;
        this.averageFuelEfficiency = averageFuelEfficiency;
    }

    // 이 바이크가 특정 사용자에게 속하는지 권한 검증
    public boolean isOwner(UUID userId) {
        return this.userEntity.getId().equals(userId);
    }

    public static BikeEntity createBikeEntity(
            UUID id,
            UserEntity userEntity,
            String manufacturerName,
            String modelName,
            Integer year,
            String category,
            Long totalMileageKm,
            boolean isExistModel
    ) {
        BikeEntity bikeEntity = new BikeEntity();
        bikeEntity.id = id == null ? UUID.randomUUID() : id;
        bikeEntity.userEntity = userEntity;
        bikeEntity.manufacturerName = manufacturerName;
        bikeEntity.modelName = modelName;
        bikeEntity.year = year;
        bikeEntity.category = category;
        bikeEntity.totalMileageKm = totalMileageKm;
        bikeEntity.isExistModel = isExistModel;
        return bikeEntity;
    }
}
