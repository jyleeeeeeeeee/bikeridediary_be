package com.bikeridediary.domain.bikemodel.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
// 모터사이클 제조사 마스터 데이터
@Entity
@Table(name = "manufacturers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManufacturerEntity {

    // 제조사명 (PK, API-Ninjas make 파라미터와 동일)
    @Id
    @Column(name = "manufacturer_name", length = 100)
    private String manufacturerName;

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

    // 로고 이미지 파일명 (static/logos/ 내 파일)
    @Column(name = "image_file", length = 200)
    private String imageFile;

    public static ManufacturerEntity create(String manufacturerName, String displayNameKo, String country, int displayOrder, String imageFile) {
        ManufacturerEntity entity = new ManufacturerEntity();
        entity.manufacturerName = manufacturerName;
        entity.displayNameKo = displayNameKo;
        entity.country = country;
        entity.displayOrder = displayOrder;
        entity.imageFile = imageFile;
        return entity;
    }

    public void update(String displayNameKo, String country, int displayOrder, boolean isActive, String imageFile) {
        this.displayNameKo = displayNameKo;
        this.country = country;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.imageFile = imageFile;
    }
}
