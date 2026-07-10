package com.bikeridediary.domain.place_category.entity;

import com.bikeridediary.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// 장소 카테고리 엔티티 - PlaceEntity가 참조하는 lookup 테이블 (LANDMARK/CAFE/SERVICE_CENTER 등, 관리자가 재배포 없이 관리)
@Entity
@Table(name = "place_categories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceCategoryEntity extends BaseEntity {

    // 카테고리 코드 (PK, 예: LANDMARK / CAFE / SERVICE_CENTER)
    @Id
    @Column(name = "category_code", length = 50)
    private String categoryCode;

    // 표시 이름 (UI 노출 문구, 예: 명소 / 바이크 카페 / 정비 센터)
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    // UI 정렬 순서 (낮을수록 앞)
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    // 카테고리 엔티티 생성
    public static PlaceCategoryEntity create(String categoryCode, String categoryName, int displayOrder) {
        PlaceCategoryEntity entity = new PlaceCategoryEntity();
        entity.categoryCode = categoryCode;
        entity.categoryName = categoryName;
        entity.displayOrder = displayOrder;
        return entity;
    }

    // 카테고리 정보 수정 (categoryCode는 PK라 변경 불가)
    public void update(String categoryName, int displayOrder) {
        this.categoryName = categoryName;
        this.displayOrder = displayOrder;
    }
}
