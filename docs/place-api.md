# Place 도메인 API 스펙

라이더용 큐레이션 POI (명소/바이크 카페/정비 센터). 주유소는 별도 `station` 도메인 유지.

## 도메인 개요

- **PlaceEntity** — 장소 본체. 좌표·주소·설명·별점·찜 수·딥링크 ID 등
- **PlaceCategoryEntity** — 카테고리 lookup 테이블 (재배포 없이 관리자가 추가/재정렬). enum 아닌 별도 도메인
- **PlaceWishEntity** — 유저별 찜 목록 (place ↔ user 복합 PK)
- **별점**: MVP는 큐레이터/관리자가 직접 넣는 정적 값(`PlaceEntity.starPoint`). 자체 리뷰 시스템 도입은 결정 유보 상태 (도입 시 place_reviews 테이블 신설 + 평균 캐시로 전환)

## 패키지 구조

```
com.bikeridediary.domain.place
├── controller/PlaceController.java
├── service/PlaceService.java
├── repository/PlaceRepository.java
├── entity/PlaceEntity.java
├── dto/
│   ├── PlaceResponse.java
│   ├── PlaceCategoryResponse.java
│   └── PlaceWishResponse.java
└── (없음, category는 별도 도메인)

com.bikeridediary.domain.place_category
├── controller/PlaceCategoryController.java  (선택, 카테고리 목록 조회용)
├── service/PlaceCategoryService.java
├── repository/PlaceCategoryRepository.java
└── entity/PlaceCategoryEntity.java

com.bikeridediary.domain.place_wish
├── controller/PlaceWishController.java
├── service/PlaceWishService.java
├── repository/PlaceWishRepository.java
├── entity/
│   ├── PlaceWishEntity.java
│   └── PlaceWishId.java   (복합 PK 클래스)
```

## PlaceCategoryEntity (place_categories 테이블)

BaseEntity 상속으로 createdAt/updatedAt/deletedAt 자동 관리.

| 필드 | 타입 | 제약 | 비고 |
|-----|------|-----|-----|
| categoryCode | String(50) | PK | 예: LANDMARK / CAFE / SERVICE_CENTER |
| categoryName | String(50) | NOT NULL | UI 노출 문구 (예: 명소 / 바이크 카페 / 정비 센터) |
| displayOrder | int | NOT NULL, default 0 | UI 정렬 순서 (낮을수록 앞) |

**팩토리**: `create(code, name, order)`, `update(name, order)` (code는 PK라 변경 불가)

## PlaceEntity (places 테이블)

BaseEntity 상속.

| 필드 | 타입 | 제약 | 비고 |
|-----|------|-----|-----|
| id | UUID | PK | |
| placeName | String(100) | NOT NULL | |
| userEntity | UserEntity | FK, nullable | 등록한 사용자. 관리자 시드 데이터는 null |
| starPoint | Float | nullable | 0.0 ~ 5.0, 소수점 1자리 표시용 (정확도 요구 낮아 Float) |
| wishedCount | int | NOT NULL, default 0 | 찜 수 캐시. place_wishes count로 갱신 |
| placeCategoryEntity | PlaceCategoryEntity | FK (category_code), NOT NULL | |
| latitude | double | NOT NULL | |
| longitude | double | NOT NULL | |
| address | String(200) | nullable | 지번 주소 |
| roadAddress | String(200) | nullable | 도로명 주소 |
| description | TEXT | nullable | 큐레이션 설명 |
| photoUrl | String(500) | nullable | 대표 사진 URL (S3) |
| phone | String(30) | nullable | |
| kakaoPlaceId | String(50) | nullable | 카카오맵 딥링크용 |
| naverPlaceId | String(50) | nullable | 네이버 지도 딥링크용 |

**팩토리**: `create(...)`, `createWithId(uuid, ...)` (sync/시드 용도)
**도메인 메서드**: `update(...)`, `updatePhotoUrl(url)`, `updateStarPoint(v)`, `incrementWishedCount()`, `decrementWishedCount()`

## PlaceWishEntity (place_wishes 테이블)

**복합 PK: (place_id, user_id)**. 소프트 삭제 없음 (찜 해제 = 하드 delete). BaseEntity **미상속** (updated_at/deleted_at 불필요, created_at만 개별 관리).

### PlaceWishId (복합 PK 클래스)

```java
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class PlaceWishId implements Serializable {
    private UUID placeId;
    private UUID userId;

    public PlaceWishId(UUID placeId, UUID userId) {
        this.placeId = placeId;
        this.userId = userId;
    }
}
```

### PlaceWishEntity

```java
@Entity
@Table(name = "place_wishes",
       indexes = {
           @Index(name = "idx_place_wishes_user", columnList = "user_id"),
           @Index(name = "idx_place_wishes_place", columnList = "place_id")
       })
@IdClass(PlaceWishId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceWishEntity {

    // 찜한 장소 (복합 PK 일부)
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private PlaceEntity placeEntity;

    // 찜한 사용자 (복합 PK 일부)
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    // 찜한 시각
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static PlaceWishEntity create(PlaceEntity placeEntity, UserEntity userEntity) {
        PlaceWishEntity entity = new PlaceWishEntity();
        entity.placeEntity = placeEntity;
        entity.userEntity = userEntity;
        return entity;
    }
}
```

## Repository

### PlaceRepository

```java
public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {

    // 전체 조회 (삭제 안 된 것만, 카테고리 정렬)
    List<PlaceEntity> findByDeletedAtIsNullOrderByPlaceCategoryEntity_DisplayOrderAsc();

    // 카테고리 필터 조회
    List<PlaceEntity> findByPlaceCategoryEntity_CategoryCodeAndDeletedAtIsNull(String categoryCode);

    // 지도 bbox 검색 (선택, 데이터 많아지면 도입)
    @Query("""
        SELECT p FROM PlaceEntity p
        WHERE p.deletedAt IS NULL
          AND (:categoryCode IS NULL OR p.placeCategoryEntity.categoryCode = :categoryCode)
          AND p.latitude BETWEEN :swLat AND :neLat
          AND p.longitude BETWEEN :swLng AND :neLng
    """)
    List<PlaceEntity> findInBbox(
        @Param("categoryCode") String categoryCode,
        @Param("swLat") double swLat, @Param("neLat") double neLat,
        @Param("swLng") double swLng, @Param("neLng") double neLng
    );
}
```

### PlaceCategoryRepository

```java
public interface PlaceCategoryRepository extends JpaRepository<PlaceCategoryEntity, String> {
    List<PlaceCategoryEntity> findByDeletedAtIsNullOrderByDisplayOrderAsc();
}
```

### PlaceWishRepository

```java
public interface PlaceWishRepository extends JpaRepository<PlaceWishEntity, PlaceWishId> {

    // 내 찜 목록 (place 카테고리 순으로 정렬)
    @Query("""
        SELECT w FROM PlaceWishEntity w
        JOIN FETCH w.placeEntity p
        JOIN FETCH p.placeCategoryEntity
        WHERE w.userEntity.id = :userId
          AND p.deletedAt IS NULL
        ORDER BY p.placeCategoryEntity.displayOrder ASC, w.createdAt DESC
    """)
    List<PlaceWishEntity> findByUserId(@Param("userId") UUID userId);

    boolean existsByPlaceEntity_IdAndUserEntity_Id(UUID placeId, UUID userId);

    void deleteByPlaceEntity_IdAndUserEntity_Id(UUID placeId, UUID userId);

    long countByPlaceEntity_Id(UUID placeId);
}
```

## Service 로직 요지

### PlaceService

- `list(String categoryCode)` — categoryCode null이면 전체, 아니면 필터
- `get(UUID placeId)` — 단건 조회 (soft delete 검사)

### PlaceWishService

```java
@Transactional
public void addWish(UUID placeId, UUID userId) {
    if (placeWishRepository.existsByPlaceEntity_IdAndUserEntity_Id(placeId, userId)) return;

    PlaceEntity place = placeRepository.findById(placeId)
        .filter(p -> !p.isDeleted())
        .orElseThrow(() -> new NotFoundException("장소 없음"));
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("사용자 없음"));

    placeWishRepository.save(PlaceWishEntity.create(place, user));
    place.incrementWishedCount();  // dirty checking으로 UPDATE
}

@Transactional
public void removeWish(UUID placeId, UUID userId) {
    if (!placeWishRepository.existsByPlaceEntity_IdAndUserEntity_Id(placeId, userId)) return;

    placeWishRepository.deleteByPlaceEntity_IdAndUserEntity_Id(placeId, userId);
    placeRepository.findById(placeId).ifPresent(PlaceEntity::decrementWishedCount);
}

public List<PlaceResponse> listMyWishes(UUID userId) {
    return placeWishRepository.findByUserId(userId).stream()
        .map(w -> PlaceResponse.from(w.getPlaceEntity()))
        .toList();
}
```

**wishedCount 정합성**: 애플리케이션에서 ±1 방식이라 이론상 race condition/누락 위험 있음. 심각해지면 (1) `UPDATE places SET wished_count = (SELECT COUNT(*) FROM place_wishes WHERE place_id = ?) WHERE id = ?` 배치, 또는 (2) DB 트리거로 전환.

## Controller 엔드포인트

### PlaceController

```java
@Tag(name = "장소", description = "라이더 큐레이션 POI (명소/카페/정비센터)")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceWishService placeWishService;

    @Operation(summary = "장소 목록 조회 (전체 또는 카테고리 필터)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> list(
            @RequestParam(required = false) String categoryCode
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.list(categoryCode)));
    }

    @Operation(summary = "장소 상세 조회")
    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponse>> get(@PathVariable UUID placeId) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.get(placeId)));
    }

    @Operation(summary = "장소 찜 추가")
    @PostMapping("/{placeId}/wish")
    public ResponseEntity<ApiResponse<Void>> addWish(
            @PathVariable UUID placeId,
            @AuthenticationPrincipal UUID userId
    ) {
        placeWishService.addWish(placeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "장소 찜 해제")
    @DeleteMapping("/{placeId}/wish")
    public ResponseEntity<ApiResponse<Void>> removeWish(
            @PathVariable UUID placeId,
            @AuthenticationPrincipal UUID userId
    ) {
        placeWishService.removeWish(placeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "내 찜 목록 조회")
    @GetMapping("/wishes/me")
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> myWishes(
            @AuthenticationPrincipal UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeWishService.listMyWishes(userId)));
    }
}
```

### PlaceCategoryController (선택)

카테고리를 서버 관리형으로 두면 앱이 이 엔드포인트로 카테고리 목록을 받아 UI 필터 배지를 그림.

```java
@Tag(name = "장소 카테고리")
@RestController
@RequestMapping("/api/v1/place-categories")
@RequiredArgsConstructor
public class PlaceCategoryController {

    private final PlaceCategoryService placeCategoryService;

    @Operation(summary = "카테고리 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceCategoryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(placeCategoryService.list()));
    }
}
```

## DTO

### PlaceResponse

```java
public record PlaceResponse(
    UUID id,
    String placeName,
    PlaceCategoryResponse category,
    double latitude,
    double longitude,
    String address,
    String roadAddress,
    String description,
    String photoUrl,
    String phone,
    Float starPoint,
    int wishedCount,
    String kakaoPlaceId,
    String naverPlaceId
) {
    public static PlaceResponse from(PlaceEntity entity) {
        return new PlaceResponse(
            entity.getId(),
            entity.getPlaceName(),
            PlaceCategoryResponse.from(entity.getPlaceCategoryEntity()),
            entity.getLatitude(), entity.getLongitude(),
            entity.getAddress(), entity.getRoadAddress(),
            entity.getDescription(), entity.getPhotoUrl(),
            entity.getPhone(),
            entity.getStarPoint(), entity.getWishedCount(),
            entity.getKakaoPlaceId(), entity.getNaverPlaceId()
        );
    }
}
```

### PlaceCategoryResponse

```java
public record PlaceCategoryResponse(
    String categoryCode,
    String categoryName,
    int displayOrder
) {
    public static PlaceCategoryResponse from(PlaceCategoryEntity entity) {
        return new PlaceCategoryResponse(
            entity.getCategoryCode(),
            entity.getCategoryName(),
            entity.getDisplayOrder()
        );
    }
}
```

## 인증

- `GET /api/v1/places/**`, `GET /api/v1/place-categories`: 로그인/게스트 모두 조회 가능 → SecurityConfig에서 `permitAll()`
- `POST/DELETE /api/v1/places/{id}/wish`, `GET /api/v1/places/wishes/me`: 로그인 필수 (JWT `@AuthenticationPrincipal UUID userId`)
- 로컬 게스트에게도 지도/카테고리 조회는 허용 (찜은 로그인 유도)

SecurityConfig 추가:
```java
.requestMatchers(HttpMethod.GET, "/api/v1/places/*/wish").authenticated()
.requestMatchers("/api/v1/places/wishes/**").authenticated()
.requestMatchers(HttpMethod.GET, "/api/v1/places/**").permitAll()
.requestMatchers(HttpMethod.GET, "/api/v1/place-categories/**").permitAll()
```
(순서 중요 — 구체적인 매칭이 먼저)

## 시드 데이터

### place_categories 먼저

```sql
INSERT INTO place_categories (category_code, category_name, display_order, created_at, updated_at) VALUES
('LANDMARK',       '명소',        1, NOW(), NOW()),
('CAFE',           '바이크 카페', 2, NOW(), NOW()),
('SERVICE_CENTER', '정비 센터',   3, NOW(), NOW());
```

### places (카테고리 FK 참조)

```sql
INSERT INTO places (id, place_name, user_id, star_point, wished_count, category_code,
                    latitude, longitude, address, road_address, description,
                    created_at, updated_at) VALUES
('a1000000-0000-0000-0000-000000000001', '북악 스카이웨이 팔각정', NULL, 4.5, 0, 'LANDMARK',
 37.6076, 126.9797, NULL, '서울 종로구 북악산로 267',
 '서울 시내를 한눈에 내려다볼 수 있는 라이더 성지. 야경 명소.', NOW(), NOW()),
('a1000000-0000-0000-0000-000000000002', '남산 팔각정', NULL, 4.3, 0, 'LANDMARK',
 37.5510, 126.9882, NULL, '서울 용산구 남산공원길 105',
 '남산 정상 명소. 저녁 라이딩 목적지로 인기.', NOW(), NOW()),
('a1000000-0000-0000-0000-000000000003', '가평 아침고요 라이더 카페', NULL, 4.6, 0, 'CAFE',
 37.7523, 127.4681, NULL, '경기 가평군 상면 수목원로 432',
 '2층 통유리 뷰. 대형 주차장, 헬멧 거치대 완비.', NOW(), NOW()),
('a1000000-0000-0000-0000-000000000004', '양평 두물머리 카페', NULL, 4.2, 0, 'CAFE',
 37.5372, 127.3236, NULL, '경기 양평군 양서면 양수리 683-1',
 '한강 뷰. 주말 라이더 모임 장소.', NOW(), NOW()),
('a1000000-0000-0000-0000-000000000005', '분당 바이크월드 정비센터', NULL, 4.7, 0, 'SERVICE_CENTER',
 37.3595, 127.1052, NULL, '경기 성남시 분당구 정자일로 95',
 '대형 바이크 종합 정비. 예약 필요.', NOW(), NOW());
```

## 구현 순서 권장

1. `PlaceCategoryEntity` + Repository + 시드 3개
2. `PlaceEntity` + Repository + 시드 5개 (카테고리 FK 연결)
3. `PlaceService.list/get` + `PlaceResponse`, `PlaceCategoryResponse` DTO
4. `PlaceController` (조회 2개) + `PlaceCategoryController` (선택)
5. SecurityConfig permitAll 추가
6. Swagger에서 조회 확인
7. `PlaceWishEntity` + `PlaceWishId` + Repository
8. `PlaceWishService` (add/remove/listMyWishes)
9. `PlaceController`에 wish 엔드포인트 3개 추가
10. SecurityConfig authenticated 매칭 추가
11. 앱 연동

## 미해결 / 유보

- **자체 리뷰 시스템 (place_reviews 테이블)**: 결정 유보. 도입 시 유저별 별점/코멘트 저장, `PlaceEntity.starPoint`는 리뷰 평균 캐시로 전환 (배치 or 트리거).
- **wishedCount 정합성**: 애플리케이션 ±1이라 이론상 이슈 있음. 위 Service 섹션의 fallback 참고.
- **주유소 카테고리**: 현재 앱 지도 UI 토글에 있으나 station 도메인과 분리. place에 GAS_STATION 카테고리 추가할지, station 그대로 유지할지 결정 필요.
