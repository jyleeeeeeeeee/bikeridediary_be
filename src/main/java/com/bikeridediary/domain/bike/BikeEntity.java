package com.bikeridediary.domain.bike;

import com.bikeridediary.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bike entity.
 *
 * MVP: manufacturer_name, model_name, year stored as plain text (direct input)
 * Phase 2: bike_trim_id FK added when bike model DB is built
 */
@Entity
@Table(name = "bikes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BikeEntity {

    // 바이크 ID (UUID)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 소유 사용자 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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

    // 바이크 카테고리 (도로, 산악, 투어링 등)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BikeCategory category;

    // 현재 총 주행거리 (km) - 사용자가 수동으로 업데이트
    @Column(name = "total_mileage_km", nullable = false)
    private Integer totalMileageKm;

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

    // 등록 일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 삭제 일시 (소프트 삭제)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static BikeEntity create(
            UserEntity userEntity,
            String manufacturerName,
            String modelName,
            Integer year,
            BikeCategory category,
            Integer totalMileageKm
    ) {
        BikeEntity bikeEntity = new BikeEntity();
        bikeEntity.userEntity = userEntity;
        bikeEntity.manufacturerName = manufacturerName;
        bikeEntity.modelName = modelName;
        bikeEntity.year = year;
        bikeEntity.category = category;
        bikeEntity.totalMileageKm = totalMileageKm;
        return bikeEntity;
    }

    public void update(
            String manufacturerName,
            String modelName,
            Integer year,
            BikeCategory category,
            Integer totalMileageKm,
            LocalDate purchasedAt,
            String memo
    ) {
        this.manufacturerName = manufacturerName;
        this.modelName = modelName;
        this.year = year;
        this.category = category;
        this.totalMileageKm = totalMileageKm;
        this.purchasedAt = purchasedAt;
        this.memo = memo;
    }

    public void setRepresentative(boolean isRepresentative) {
        this.isRepresentative = isRepresentative;
    }

    public void updatePhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void updateMileage(int mileageKm) {
        this.totalMileageKm = mileageKm;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isOwner(UUID userId) {
        return this.userEntity.getId().equals(userId);
    }
}
