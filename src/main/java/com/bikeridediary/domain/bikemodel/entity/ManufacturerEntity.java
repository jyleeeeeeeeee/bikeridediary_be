package com.bikeridediary.domain.bikemodel.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 모터사이클 제조사 마스터 데이터 — API-Ninjas 연동 기반
@Entity
@Table(name = "manufacturers")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManufacturerEntity {

    // 제조사 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // API 호출용 제조사명 (예: "Honda", "kymco")
    @Column(name = "api_name", nullable = false, unique = true, length = 100)
    private String apiName;

    // 한글 표시명 (예: "혼다", "킴코")
    @Column(name = "display_name_ko", nullable = false, length = 100)
    private String displayNameKo;

    // 국가
    @Column(length = 50)
    private String country;

    // 활성화 여부 (앱에 노출할지)
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // UI 정렬 순서 (낮을수록 앞에 표시)
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 999;

    // 등록 일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 일시
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static ManufacturerEntity create(String apiName, String displayNameKo, String country, int displayOrder) {
        ManufacturerEntity entity = new ManufacturerEntity();
        entity.apiName = apiName;
        entity.displayNameKo = displayNameKo;
        entity.country = country;
        entity.displayOrder = displayOrder;
        return entity;
    }

    public void update(String displayNameKo, String country, int displayOrder, boolean isActive) {
        this.displayNameKo = displayNameKo;
        this.country = country;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }
}
