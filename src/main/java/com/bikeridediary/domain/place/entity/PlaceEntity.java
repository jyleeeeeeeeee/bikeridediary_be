package com.bikeridediary.domain.place.entity;

import com.bikeridediary.domain.common.entity.BaseEntity;
import com.bikeridediary.domain.place_category.entity.PlaceCategoryEntity;
import com.bikeridediary.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

// 장소 엔티티 - 라이더용 큐레이션 POI (명소/카페/정비센터). 카테고리는 별도 lookup 테이블(PlaceCategoryEntity)로 관리.
@Entity
@Table(name = "places")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceEntity extends BaseEntity {

    // 장소 ID (UUID)
    @Id
    @Column(name = "id")
    private UUID id;

    // 장소 이름
    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    // 등록한 사용자 (FK, nullable - 큐레이션 장소는 관리자 시드라 null 허용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    // 별점 (0.0 ~ 5.0, 소수점 1자리 표시. 정확도 요구 낮아 Float 사용)
    @Column(name = "star_point")
    private Float starPoint;

    // 찜한 사용자 수
    @Column(name = "wished_count", nullable = false)
    private int wishedCount = 0;

    // 카테고리 (FK, PlaceCategoryEntity.category_code)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", nullable = false)
    private PlaceCategoryEntity placeCategoryEntity;

    // 위도
    @Column(name = "latitude", nullable = false)
    private double latitude;

    // 경도
    @Column(name = "longitude", nullable = false)
    private double longitude;

    // 지번 주소
    @Column(name = "address", length = 200)
    private String address;

    // 도로명 주소
    @Column(name = "road_address", length = 200)
    private String roadAddress;

    // 큐레이션 설명 (긴 텍스트, TEXT 컬럼)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 대표 사진 URL (S3)
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    // 연락처
    @Column(name = "phone", length = 30)
    private String phone;

    // 카카오맵 딥링크용 ID
    @Column(name = "kakao_place_id", length = 50)
    private String kakaoPlaceId;

    // 네이버 지도 딥링크용 ID
    @Column(name = "naver_place_id", length = 50)
    private String naverPlaceId;

    // 장소 엔티티 생성 (UUID 자동 생성)
    public static PlaceEntity create(
            String placeName,
            UserEntity userEntity,
            PlaceCategoryEntity placeCategoryEntity,
            double latitude,
            double longitude,
            String address,
            String roadAddress,
            String description,
            String photoUrl,
            String phone,
            String kakaoPlaceId,
            String naverPlaceId
    ) {
        return createPlaceEntity(null, placeName, userEntity, placeCategoryEntity,
                latitude, longitude, address, roadAddress, description,
                photoUrl, phone, kakaoPlaceId, naverPlaceId);
    }

    // 지정 UUID로 장소 엔티티 생성 (시드/sync 용도)
    public static PlaceEntity createWithId(
            UUID id,
            String placeName,
            UserEntity userEntity,
            PlaceCategoryEntity placeCategoryEntity,
            double latitude,
            double longitude,
            String address,
            String roadAddress,
            String description,
            String photoUrl,
            String phone,
            String kakaoPlaceId,
            String naverPlaceId
    ) {
        return createPlaceEntity(id, placeName, userEntity, placeCategoryEntity,
                latitude, longitude, address, roadAddress, description,
                photoUrl, phone, kakaoPlaceId, naverPlaceId);
    }

    // 장소 정보 수정
    public void update(
            String placeName,
            PlaceCategoryEntity placeCategoryEntity,
            double latitude,
            double longitude,
            String address,
            String roadAddress,
            String description,
            String photoUrl,
            String phone,
            String kakaoPlaceId,
            String naverPlaceId
    ) {
        this.placeName = placeName;
        this.placeCategoryEntity = placeCategoryEntity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.roadAddress = roadAddress;
        this.description = description;
        this.photoUrl = photoUrl;
        this.phone = phone;
        this.kakaoPlaceId = kakaoPlaceId;
        this.naverPlaceId = naverPlaceId;
    }

    // 대표 사진 URL 갱신
    public void updatePhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // 별점 갱신 (관리자용 - 유저 리뷰 집계 로직 별도 구현 시 사용)
    public void updateStarPoint(Float starPoint) {
        this.starPoint = starPoint;
    }

    // 찜 카운트 증가 (유저가 찜할 때)
    public void incrementWishedCount() {
        this.wishedCount++;
    }

    // 찜 카운트 감소 (유저가 찜 해제할 때)
    public void decrementWishedCount() {
        if (this.wishedCount > 0) {
            this.wishedCount--;
        }
    }

    private static PlaceEntity createPlaceEntity(
            UUID id,
            String placeName,
            UserEntity userEntity,
            PlaceCategoryEntity placeCategoryEntity,
            double latitude,
            double longitude,
            String address,
            String roadAddress,
            String description,
            String photoUrl,
            String phone,
            String kakaoPlaceId,
            String naverPlaceId
    ) {
        PlaceEntity placeEntity = new PlaceEntity();
        placeEntity.id = id == null ? UUID.randomUUID() : id;
        placeEntity.placeName = placeName;
        placeEntity.userEntity = userEntity;
        placeEntity.placeCategoryEntity = placeCategoryEntity;
        placeEntity.latitude = latitude;
        placeEntity.longitude = longitude;
        placeEntity.address = address;
        placeEntity.roadAddress = roadAddress;
        placeEntity.description = description;
        placeEntity.photoUrl = photoUrl;
        placeEntity.phone = phone;
        placeEntity.kakaoPlaceId = kakaoPlaceId;
        placeEntity.naverPlaceId = naverPlaceId;
        return placeEntity;
    }
}
